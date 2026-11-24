// ============================================================================
// LE SERVICE DROPLET ID — ce téléphone est l'appareil qui approuve les
// connexions. Même API que l'app Droplet :
//
//   POST   /v1/comptes                    créer le compte (clé neuve)
//   GET    /v1/demandes/{id}              lire une demande de connexion
//   POST   /v1/demandes/{id}/decision     approuver (avec le nombre) ou refuser
//   GET    /v1/connexions                 les apps et sites connectés
//   DELETE /v1/connexions/{client}        retirer un accès
//   DELETE /v1/appareils/{id}             oublier ce téléphone
//
// La graine Ed25519 est PROPRE à cet appareil et à ce service ; elle n'est
// écrite dans le coffre qu'une fois le compte accepté par le serveur.
// Le service ne reçoit jamais : numéro, e-mail, nom, contacts.
// ============================================================================
package app.dropletmesh.id.donnees

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.TimeUnit

class ServiceDropletId(val emetteur: String, private val coffre: Coffre) {

    val estTest: Boolean get() = emetteur == Liens.TEST
    private val prefixe = if (estTest) "test" else "prod"
    private val cleGraine = "${prefixe}_graine_ed25519"
    private val cleAppareil = "${prefixe}_appareil"
    private val cleCompte = "${prefixe}_compte"
    private val cleCreeLe = "${prefixe}_cree_le"

    fun estActive(): Boolean = coffre.contient(cleAppareil)

    fun compteId(): String? = coffre.lireTexte(cleCompte)
    fun appareilId(): String? = coffre.lireTexte(cleAppareil)
    fun creeLe(): Instant? = coffre.lireTexte(cleCreeLe)?.toLongOrNull()?.let(Instant::ofEpochSecond)

    /** Crée le compte (une seule fois) : une clé neuve, rien d'autre. */
    suspend fun activer(nomAppareil: String) {
        if (estActive()) return
        val graine = ByteArray(32).also(SecureRandom()::nextBytes)
        val r = appeler("POST", "/v1/comptes", JSONObject().put("nom", nomAppareil), graine = graine, clePublique = Protocole.clePublique(graine))
        coffre.ecrire(cleGraine, graine)
        coffre.ecrireTexte(cleAppareil, r.getString("appareil_id"))
        coffre.ecrireTexte(cleCompte, r.getString("compte_id"))
        coffre.ecrireTexte(cleCreeLe, Instant.now().epochSecond.toString())
        graine.fill(0)
    }

    suspend fun lireDemande(id: String): Demande {
        val r = appeler("GET", "/v1/demandes/$id", null)
        val client = r.getJSONObject("client")
        val logo = client.optString("logo").takeIf { it.startsWith("$emetteur/logo/") }
        return Demande(
            id = r.getString("id"),
            nomApp = client.optString("nom").ifEmpty { client.getString("id") },
            site = client.optString("site"),
            portees = r.getJSONArray("portees").chaines(),
            expireLe = Instant.parse(r.getString("expire_le")),
            logo = logo,
        )
    }

    suspend fun approuver(id: String, code: String, portees: List<String>, pseudo: String?) {
        val corps = JSONObject()
            .put("decision", "approuver")
            .put("code", code)
            .put("portees", JSONArray(portees))
        pseudo?.trim()?.takeIf { it.isNotEmpty() }?.let { corps.put("pseudo", it) }
        appeler("POST", "/v1/demandes/$id/decision", corps)
    }

    suspend fun refuser(id: String) {
        appeler("POST", "/v1/demandes/$id/decision", JSONObject().put("decision", "refuser"))
    }

    suspend fun connexions(): List<Connexion> {
        val r = appeler("GET", "/v1/connexions", null)
        val liste = r.getJSONArray("connexions")
        return (0 until liste.length()).map { i ->
            val c = liste.getJSONObject(i)
            Connexion(
                clientId = c.getString("client_id"),
                nom = c.getString("nom"),
                portees = c.getJSONArray("portees").chaines(),
                accordeLe = Instant.parse(c.getString("accorde_le")),
            )
        }
    }

    suspend fun retirer(clientId: String) {
        appeler("DELETE", "/v1/connexions/${encoder(clientId)}", null)
    }

    /** Oublie Droplet ID sur ce téléphone (retire l'appareil côté service). */
    suspend fun oublier() {
        appareilId()?.let { appareil ->
            try {
                appeler("DELETE", "/v1/appareils/$appareil", null)
            } catch (_: ErreurDropletId) {
                // Hors ligne ou déjà retiré : on oublie quand même ici.
            }
        }
        coffre.effacer(cleGraine, cleAppareil, cleCompte, cleCreeLe)
    }

    // ── La requête signée ──────────────────────────────────────────────
    private suspend fun appeler(
        methode: String,
        chemin: String,
        corps: JSONObject?,
        graine: ByteArray? = null,
        clePublique: String? = null,
    ): JSONObject = withContext(Dispatchers.IO) {
        val texte = corps?.toString() ?: ""
        val g = graine ?: coffre.lire(cleGraine) ?: throw ErreurDropletId("pas_de_compte")
        val entetes = Protocole.entetes(
            graine = g,
            methode = methode,
            chemin = chemin,
            corps = texte,
            appareilId = if (clePublique == null) appareilId() else null,
            clePublique = clePublique,
        )
        if (graine == null) g.fill(0)
        val constructeur = Request.Builder().url(emetteur + chemin)
        entetes.forEach { (k, v) -> constructeur.header(k, v) }
        val json = "application/json".toMediaType()
        when (methode) {
            "GET" -> constructeur.get()
            "DELETE" -> constructeur.delete()
            else -> constructeur.post(texte.toRequestBody(json))
        }
        val reponse = try {
            client.newCall(constructeur.build()).execute()
        } catch (_: IOException) {
            throw ErreurDropletId("reseau")
        }
        reponse.use { r ->
            val brut = r.body?.string().orEmpty()
            val j = if (brut.isBlank()) JSONObject() else runCatching { JSONObject(brut) }.getOrElse { JSONObject() }
            if (r.code >= 400) throw ErreurDropletId(j.optString("erreur").ifEmpty { "http_${r.code}" }, r.code)
            j
        }
    }

    private fun JSONArray.chaines(): List<String> = (0 until length()).map(::getString)

    private fun encoder(s: String): String = java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    companion object {
        private val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}

data class Demande(
    val id: String,
    val nomApp: String,
    val site: String,
    val portees: List<String>,
    val expireLe: Instant,
    val logo: String?,
) {
    val demandeProfil: Boolean get() = "profile" in portees
}

data class Connexion(
    val clientId: String,
    val nom: String,
    val portees: List<String>,
    val accordeLe: Instant,
) {
    val partageProfil: Boolean get() = "profile" in portees
}

class ErreurDropletId(val code: String, val statut: Int? = null) : Exception(code) {
    /** Un message clair pour l'écran : ce qui s'est passé et quoi faire. */
    val texte: String
        get() = when (code) {
            "demande_expiree" -> "Cette demande a expiré. Recommencez sur le site."
            "mauvais_code" -> "Ce n'était pas le bon nombre. La demande est annulée par sécurité."
            "appareil_revoque" -> "Ce téléphone a été retiré de votre compte Droplet ID."
            "reseau" -> "Pas de connexion au service Droplet ID. Vérifiez Internet."
            "pas_de_compte" -> "Droplet ID n'est pas activé sur ce téléphone."
            else -> "Droplet ID a refusé la demande ($code)."
        }
}
