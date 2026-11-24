// ============================================================================
// LES CAPTURES (version debug seulement, absente de la version publiée).
// Affiche un écran avec des données d'EXEMPLE (aucun appel au serveur, aucun
// compte créé), pour photographier l'interface sur l'émulateur de la CI :
//
//   adb shell am start -n app.dropletmesh.id/.CapturesActivity --es ecran accueil --ez sombre false
// ============================================================================
package app.dropletmesh.id

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dropletmesh.id.donnees.Apparence
import app.dropletmesh.id.donnees.Connexion
import app.dropletmesh.id.donnees.Demande
import app.dropletmesh.id.donnees.EtatAppareil
import app.dropletmesh.id.donnees.LienDemande
import app.dropletmesh.id.donnees.Liens
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Onglets
import app.dropletmesh.id.designsystem.ThemeDroplet
import app.dropletmesh.id.ui.APropos
import app.dropletmesh.id.ui.Acces
import app.dropletmesh.id.ui.Accueil
import app.dropletmesh.id.ui.Approbation
import app.dropletmesh.id.ui.Bienvenue
import app.dropletmesh.id.ui.Compte
import app.dropletmesh.id.ui.Confidentialite
import app.dropletmesh.id.ui.DetailConnexion
import app.dropletmesh.id.ui.Identite
import app.dropletmesh.id.ui.Navigation
import app.dropletmesh.id.ui.Onglet
import app.dropletmesh.id.ui.Presentation
import app.dropletmesh.id.ui.ReglageApparence
import app.dropletmesh.id.ui.Securite
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost
import io.github.neoruaa.glasmio.ui.GlasmioNavigationBar
import io.github.neoruaa.glasmio.ui.GlasmioTabItem
import java.time.Instant
import java.time.temporal.ChronoUnit

class CapturesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ecran = intent.getStringExtra("ecran") ?: "accueil"
        val sombre = intent.getBooleanExtra("sombre", false)
        setContent {
            ThemeDroplet(sombre = sombre) {
                GlasmioBackdropHost(sampledColor = Droplet.couleurs.fond, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().background(Droplet.couleurs.fond)) { Ecran(ecran) }
                }
            }
        }
    }
}

private val maintenant = Instant.now()

private val exemple = UiApp(
    presentationVue = true,
    apparence = Apparence.Systeme,
    reduireTransparence = false,
    compte = EtatCompte(true, "k7Q92xAbLm4Tz8Wc", "appareil-exemple", maintenant.minus(12, ChronoUnit.DAYS)),
    appareil = EtatAppareil("Pixel 8", "15", verrouEcran = true, biometrie = false, strongBox = true),
    connexions = Chargement.Pret(
        listOf(
            Connexion("kunfox", "Kunfox", listOf("openid", "profile"), maintenant.minus(2, ChronoUnit.HOURS)),
            Connexion("drop", "Droplet Drop", listOf("openid"), maintenant.minus(3, ChronoUnit.DAYS)),
            Connexion("messenger", "Droplet Messenger", listOf("openid", "profile"), maintenant.minus(11, ChronoUnit.DAYS)),
        ),
        maintenant,
    ),
)

private val demande = EtatDemande(
    lien = LienDemande(Liens.PRODUCTION, "exempleexempleexemple"),
    depuisLien = false,
    demande = Demande("d1", "Kunfox", "kunfox.app", listOf("openid", "profile"), maintenant.plusSeconds(300), null),
)

@Composable
private fun Ecran(nom: String) {
    val nav = remember { Navigation(ouvrir = {}, retour = {}, onglet = {}) }
    when (nom) {
        "presentation" -> Presentation(onTerminer = {})
        "bienvenue" -> Bienvenue(exemple.copy(compte = EtatCompte(false, null, null, null)), onActiver = {})
        "accueil" -> AvecBarre(Onglet.Accueil) { Accueil(exemple, nav, {}) }
        "accueil-action" -> AvecBarre(Onglet.Accueil) { Accueil(exemple.copy(appareil = exemple.appareil.copy(verrouEcran = false)), nav, {}) }
        "identite" -> AvecBarre(Onglet.Identite) { Identite(exemple) }
        "acces" -> AvecBarre(Onglet.Acces) { Acces(exemple, nav, {}) }
        "acces-vide" -> AvecBarre(Onglet.Acces) { Acces(exemple.copy(connexions = Chargement.Pret(emptyList(), maintenant)), nav, {}) }
        "detail" -> DetailConnexion(exemple, "kunfox", nav, {})
        "securite" -> AvecBarre(Onglet.Securite) { Securite(exemple, nav, {}) }
        "compte" -> AvecBarre(Onglet.Compte) { Compte(exemple, nav) }
        "apparence" -> ReglageApparence(exemple, nav, {}, {})
        "confidentialite" -> Confidentialite(nav)
        "apropos" -> APropos(nav)
        "approbation" -> Approbation(demande, exemple.appareil, { _, _, _ -> }, {}, {})
        "approuvee" -> Approbation(demande.copy(resultat = EtatDemande.Resultat.Approuvee), exemple.appareil, { _, _, _ -> }, {}, {})
        else -> AvecBarre(Onglet.Accueil) { Accueil(exemple, nav, {}) }
    }
}

@Composable
private fun AvecBarre(onglet: Onglet, contenu: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        contenu()
        GlasmioNavigationBar(
            items = listOf(
                GlasmioTabItem(Onglet.Accueil, Onglet.Accueil.titre, Onglets.accueil),
                GlasmioTabItem(Onglet.Identite, Onglet.Identite.titre, Onglets.identite),
                GlasmioTabItem(Onglet.Acces, Onglet.Acces.titre, Onglets.acces),
                GlasmioTabItem(Onglet.Securite, Onglet.Securite.titre, Onglets.securite),
                GlasmioTabItem(Onglet.Compte, Onglet.Compte.titre, Onglets.compte),
            ),
            selectedKey = onglet,
            onSelected = {},
            onExpand = {},
            compactProgress = remember { mutableFloatStateOf(0f) },
            compactSize = 48.dp,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
