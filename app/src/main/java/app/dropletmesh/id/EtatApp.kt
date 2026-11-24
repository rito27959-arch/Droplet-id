// ============================================================================
// L'ÉTAT DE L'APP — une seule source de vérité pour tous les écrans.
// Les actions réseau passent par ici ; les écrans n'ont que des fonctions
// sans état (UiState en entrée, événements en sortie).
// ============================================================================
package app.dropletmesh.id

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.dropletmesh.id.donnees.Apparence
import app.dropletmesh.id.donnees.Coffre
import app.dropletmesh.id.donnees.Connexion
import app.dropletmesh.id.donnees.Demande
import app.dropletmesh.id.donnees.ErreurDropletId
import app.dropletmesh.id.donnees.EtatAppareil
import app.dropletmesh.id.donnees.LienDemande
import app.dropletmesh.id.donnees.Liens
import app.dropletmesh.id.donnees.Reglages
import app.dropletmesh.id.donnees.ServiceDropletId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class EtatCompte(
    val active: Boolean,
    val compteId: String?,
    val appareilId: String?,
    val creeLe: Instant?,
)

/** Une liste chargée depuis le service : en cours, prête, ou en échec. */
sealed interface Chargement<out T> {
    data object EnCours : Chargement<Nothing>
    data class Pret<T>(val valeur: T, val aJour: Instant) : Chargement<T>
    data class Echec(val erreur: ErreurDropletId) : Chargement<Nothing>
}

/** Une demande de connexion ouverte (scannée ou reçue par lien). */
data class EtatDemande(
    val lien: LienDemande,
    val depuisLien: Boolean,
    val demande: Demande? = null,
    val erreur: String? = null,
    val occupe: Boolean = false,
    val resultat: Resultat? = null,
) {
    enum class Resultat { Approuvee, Refusee }
}

data class UiApp(
    val presentationVue: Boolean,
    val apparence: Apparence,
    val reduireTransparence: Boolean,
    val compte: EtatCompte,
    val activation: Boolean = false,
    val erreurActivation: String? = null,
    val appareil: EtatAppareil,
    val connexions: Chargement<List<Connexion>> = Chargement.EnCours,
    val demande: EtatDemande? = null,
    /** Un message bref (succès d'une action) affiché puis effacé. */
    val message: String? = null,
)

class EtatApp(application: Application) : AndroidViewModel(application) {
    private val coffre = Coffre(application)
    private val reglages = Reglages(application)
    private val production = ServiceDropletId(Liens.PRODUCTION, coffre)
    private val test = ServiceDropletId(Liens.TEST, coffre)

    private val _ui = MutableStateFlow(
        UiApp(
            presentationVue = reglages.presentationVue,
            apparence = reglages.apparence,
            reduireTransparence = reglages.reduireTransparence,
            compte = lireCompte(),
            appareil = EtatAppareil.lire(application),
        ),
    )
    val ui: StateFlow<UiApp> = _ui.asStateFlow()

    init {
        if (_ui.value.compte.active) chargerConnexions()
    }

    private fun lireCompte() = EtatCompte(
        active = production.estActive(),
        compteId = production.compteId(),
        appareilId = production.appareilId(),
        creeLe = production.creeLe(),
    )

    private fun service(emetteur: String) = if (emetteur == Liens.TEST) test else production

    /** Relu au retour dans l'app : l'utilisateur a pu changer son verrouillage. */
    fun rafraichirAppareil() = _ui.update { it.copy(appareil = EtatAppareil.lire(getApplication())) }

    fun terminerPresentation() {
        reglages.presentationVue = true
        _ui.update { it.copy(presentationVue = true) }
    }

    fun changerApparence(a: Apparence) {
        reglages.apparence = a
        _ui.update { it.copy(apparence = a) }
    }

    fun changerTransparence(reduire: Boolean) {
        reglages.reduireTransparence = reduire
        _ui.update { it.copy(reduireTransparence = reduire) }
    }

    fun activer() {
        if (_ui.value.activation) return
        _ui.update { it.copy(activation = true, erreurActivation = null) }
        viewModelScope.launch {
            try {
                production.activer(_ui.value.appareil.modele)
                _ui.update { it.copy(activation = false, compte = lireCompte()) }
                chargerConnexions()
            } catch (e: ErreurDropletId) {
                _ui.update { it.copy(activation = false, erreurActivation = e.texte) }
            }
        }
    }

    fun chargerConnexions() {
        viewModelScope.launch {
            val avant = _ui.value.connexions
            if (avant !is Chargement.Pret) _ui.update { it.copy(connexions = Chargement.EnCours) }
            val nouveau = try {
                Chargement.Pret(production.connexions().sortedByDescending { it.accordeLe }, Instant.now())
            } catch (e: ErreurDropletId) {
                // Hors ligne : on garde la dernière liste connue, avec son heure.
                if (avant is Chargement.Pret && e.code == "reseau") avant else Chargement.Echec(e)
            }
            _ui.update { it.copy(connexions = nouveau) }
        }
    }

    fun retirer(c: Connexion) {
        viewModelScope.launch {
            try {
                production.retirer(c.clientId)
                _ui.update { u ->
                    val liste = (u.connexions as? Chargement.Pret)?.valeur.orEmpty().filterNot { it.clientId == c.clientId }
                    u.copy(connexions = Chargement.Pret(liste, Instant.now()), message = "${c.nom} n'a plus accès à votre identité.")
                }
            } catch (e: ErreurDropletId) {
                _ui.update { it.copy(message = e.texte) }
            }
        }
    }

    fun oublierCeTelephone() {
        viewModelScope.launch {
            production.oublier()
            _ui.update { it.copy(compte = lireCompte(), connexions = Chargement.EnCours, message = "Droplet ID est oublié sur ce téléphone.") }
        }
    }

    fun messageVu() = _ui.update { it.copy(message = null) }

    // ── Les demandes de connexion ───────────────────────────────────────
    fun ouvrirDemande(lien: LienDemande, depuisLien: Boolean) {
        _ui.update { it.copy(demande = EtatDemande(lien, depuisLien)) }
        viewModelScope.launch {
            val s = service(lien.emetteur)
            try {
                // Une demande du service de test sur un téléphone neuf : on
                // crée son compte de test (séparé de la production).
                if (!s.estActive()) s.activer(_ui.value.appareil.modele)
                val d = s.lireDemande(lien.demandeId)
                majDemande { it.copy(demande = d) }
            } catch (e: ErreurDropletId) {
                majDemande { it.copy(erreur = e.texte) }
            }
        }
    }

    fun approuver(code: String, partagerPseudo: Boolean, pseudo: String) {
        val etat = _ui.value.demande ?: return
        val d = etat.demande ?: return
        majDemande { it.copy(occupe = true, erreur = null) }
        viewModelScope.launch {
            try {
                service(etat.lien.emetteur).approuver(
                    d.id,
                    code = code,
                    portees = d.portees.filter { it != "profile" || partagerPseudo },
                    pseudo = if (partagerPseudo) pseudo else null,
                )
                majDemande { it.copy(occupe = false, resultat = EtatDemande.Resultat.Approuvee) }
                chargerConnexions()
            } catch (e: ErreurDropletId) {
                majDemande { it.copy(occupe = false, erreur = e.texte) }
            }
        }
    }

    fun refuser() {
        val etat = _ui.value.demande ?: return
        val d = etat.demande
        majDemande { it.copy(occupe = true) }
        viewModelScope.launch {
            if (d != null) runCatching { service(etat.lien.emetteur).refuser(d.id) }
            majDemande { it.copy(occupe = false, resultat = EtatDemande.Resultat.Refusee) }
        }
    }

    fun fermerDemande() = _ui.update { it.copy(demande = null) }

    private fun majDemande(f: (EtatDemande) -> EtatDemande) = _ui.update { u -> u.copy(demande = u.demande?.let(f)) }
}
