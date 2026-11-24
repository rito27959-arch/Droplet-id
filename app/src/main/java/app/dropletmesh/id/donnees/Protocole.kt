// ============================================================================
// LE PROTOCOLE DES REQUÊTES SIGNÉES DE DROPLET ID — le même que l'app
// Droplet (lib/features/droplet_id/protocole.dart) et que le serveur
// (signature.js) :
//
//   texte signé = MÉTHODE \n chemin \n heure \n nonce \n sha256-hex(corps)
//   signature   = Ed25519(clé de l'appareil) — base64url sans « = »
//
// En-têtes : x-droplet-ts, x-droplet-nonce, x-droplet-sig, et
// x-droplet-appareil (appareil enregistré) ou x-droplet-cle (création).
// ============================================================================
package app.dropletmesh.id.donnees

import com.google.crypto.tink.subtle.Ed25519Sign
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object Protocole {

    fun b64url(octets: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(octets)

    fun sha256Hex(texte: String): String =
        MessageDigest.getInstance("SHA-256").digest(texte.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    fun texteASigner(methode: String, chemin: String, heure: Long, nonce: String, corps: String): String =
        "$methode\n$chemin\n$heure\n$nonce\n${sha256Hex(corps)}"

    fun nonce(alea: SecureRandom = SecureRandom()): String = b64url(ByteArray(16).also(alea::nextBytes))

    /** Les en-têtes d'une requête signée par la graine Ed25519 [graine] (32 octets). */
    fun entetes(
        graine: ByteArray,
        methode: String,
        chemin: String,
        corps: String,
        appareilId: String?,
        clePublique: String?,
        heure: Long = System.currentTimeMillis() / 1000,
        nonce: String = nonce(),
    ): Map<String, String> {
        require((appareilId == null) != (clePublique == null)) { "appareil OU clé publique" }
        val signature = Ed25519Sign(graine).sign(texteASigner(methode, chemin, heure, nonce, corps).toByteArray(Charsets.UTF_8))
        return buildMap {
            put("content-type", "application/json")
            put("x-droplet-ts", heure.toString())
            put("x-droplet-nonce", nonce)
            put("x-droplet-sig", b64url(signature))
            if (clePublique != null) put("x-droplet-cle", clePublique) else put("x-droplet-appareil", appareilId!!)
        }
    }

    /** La clé publique (base64url) qui correspond à la graine. */
    fun clePublique(graine: ByteArray): String = b64url(Ed25519Sign.KeyPair.newKeyPairFromSeed(graine).publicKey)
}

/** Le service et la demande d'un lien https://id[-test].dropletmesh.app/a/<id>. */
data class LienDemande(val emetteur: String, val demandeId: String)

object Liens {
    const val PRODUCTION = "https://id.dropletmesh.app"
    const val TEST = "https://id-test.dropletmesh.app"
    private val hotes = mapOf("id.dropletmesh.app" to PRODUCTION, "id-test.dropletmesh.app" to TEST)
    private val idValide = Regex("^[A-Za-z0-9_-]{16,64}$")

    /** Null si le texte n'est pas une demande de connexion Droplet ID. */
    fun demande(texte: String): LienDemande? {
        val t = texte.trim()
        if (!t.startsWith("https://")) return null
        val reste = t.removePrefix("https://")
        val hote = reste.substringBefore('/')
        val emetteur = hotes[hote] ?: return null
        val chemin = reste.substringAfter('/', "").substringBefore('?').substringBefore('#')
        val segments = chemin.split('/')
        if (segments.size != 2 || segments[0] != "a" || !idValide.matches(segments[1])) return null
        return LienDemande(emetteur, segments[1])
    }
}
