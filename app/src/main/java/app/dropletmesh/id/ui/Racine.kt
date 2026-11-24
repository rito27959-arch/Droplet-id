// ============================================================================
// LA RACINE — thème, verre, et ce qu'on montre :
//   présentation (une seule fois) → bienvenue (activer) → la page du compte,
//   d'où s'ouvrent les autres écrans (pas d'onglets).
// Une demande de connexion (scannée ou reçue par lien) passe par-dessus tout.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dropletmesh.id.EtatApp
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.donnees.Apparence
import app.dropletmesh.id.donnees.LienDemande
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.ThemeDroplet
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost
import kotlinx.coroutines.delay

/** Les écrans poussés au-dessus d'un onglet (niveau 2 ou 3). */
sealed interface Ecran {
    data object Identite : Ecran
    data object Securite : Ecran
    data object Acces : Ecran
    data object Scanner : Ecran
    data class Connexion(val clientId: String) : Ecran
    data object Apparence : Ecran
    data object Confidentialite : Ecran
    data object APropos : Ecran
}

/** Ce que les écrans peuvent demander à la navigation. */
class Navigation(
    val ouvrir: (Ecran) -> Unit,
    val retour: () -> Unit,
)

@Composable
fun Racine(etat: EtatApp, lienEntrant: LienDemande?, lienTraite: () -> Unit, revenirAuNavigateur: () -> Unit) {
    val ui by etat.ui.collectAsStateWithLifecycle()
    val sombre = when (ui.apparence) {
        Apparence.Systeme -> isSystemInDarkTheme()
        Apparence.Clair -> false
        Apparence.Sombre -> true
    }
    // Un lien « Ouvrir dans Droplet ID » : la demande s'ouvre, l'app ensuite
    // rend la main au navigateur une fois la décision prise.
    LaunchedEffect(lienEntrant) {
        if (lienEntrant != null) {
            etat.ouvrirDemande(lienEntrant, depuisLien = true)
            lienTraite()
        }
    }
    ThemeDroplet(sombre = sombre, reduireTransparence = ui.reduireTransparence) {
        GlasmioBackdropHost(
            modifier = Modifier.fillMaxSize(),
            sampledContent = { Box(Modifier.fillMaxSize().background(Droplet.couleurs.fond)) },
        ) {
            AnimatedContent(
                targetState = when {
                    !ui.presentationVue -> 0
                    !ui.compte.active -> 1
                    else -> 2
                },
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "etape",
            ) { etape ->
                when (etape) {
                    0 -> Presentation(onTerminer = etat::terminerPresentation)
                    1 -> Bienvenue(ui, onActiver = etat::activer)
                    else -> Principal(ui, etat)
                }
            }
            AnimatedVisibility(
                visible = ui.demande != null,
                enter = slideInVertically(tween(320)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(260)) { it } + fadeOut(tween(200)),
            ) {
                ui.demande?.let { d ->
                    Approbation(
                        etat = d,
                        appareil = ui.appareil,
                        onApprouver = etat::approuver,
                        onRefuser = etat::refuser,
                        onFermer = {
                            val revenir = d.depuisLien && d.resultat != null
                            etat.fermerDemande()
                            if (revenir) revenirAuNavigateur()
                        },
                    )
                }
            }
            Message(ui.message, etat::messageVu, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun Principal(ui: UiApp, etat: EtatApp) {
    val pile = remember { mutableStateListOf<Ecran>() }
    val nav = remember {
        Navigation(
            ouvrir = { pile.add(it) },
            retour = { if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex) },
        )
    }
    BackHandler(enabled = pile.isNotEmpty()) { nav.retour() }

    Box(Modifier.fillMaxSize().background(Droplet.couleurs.fond)) {
        Accueil(ui, nav, onOublier = etat::oublierCeTelephone)

        // Les écrans poussés glissent depuis la droite (300 ms, décélération).
        val dessus = pile.lastOrNull()
        AnimatedContent(
            targetState = dessus,
            transitionSpec = {
                val entre = targetState != null && pile.size > 0 && (initialState == null || pile.indexOf(initialState) < pile.lastIndex)
                if (entre) {
                    slideInHorizontally(tween(300, easing = Decelere)) { it } togetherWith slideOutHorizontally(tween(300, easing = Decelere)) { -it / 4 }
                } else {
                    slideInHorizontally(tween(300, easing = Decelere)) { -it / 4 } togetherWith slideOutHorizontally(tween(300, easing = Decelere)) { it }
                }
            },
            label = "pile",
        ) { e ->
            if (e == null) return@AnimatedContent
            Box(Modifier.fillMaxSize().background(Droplet.couleurs.fond)) {
                when (e) {
                    Ecran.Identite -> Identite(ui, nav)
                    Ecran.Securite -> Securite(ui, nav, onOublier = { pile.clear(); etat.oublierCeTelephone() })
                    Ecran.Acces -> Acces(ui, nav, onRafraichir = etat::chargerConnexions)
                    Ecran.Scanner -> Scanner(
                        onLien = { lien -> nav.retour(); etat.ouvrirDemande(lien, depuisLien = false) },
                        onFermer = nav.retour,
                    )
                    is Ecran.Connexion -> DetailConnexion(ui, e.clientId, nav, onRetirer = { c -> etat.retirer(c); nav.retour() })
                    Ecran.Apparence -> ReglageApparence(ui, nav, etat::changerApparence, etat::changerTransparence)
                    Ecran.Confidentialite -> Confidentialite(nav)
                    Ecran.APropos -> APropos(nav)
                }
            }
        }
    }
}

/** La courbe de Droplet Design : décélère, ne dépasse jamais. */
private val Decelere = androidx.compose.animation.core.CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

/** Un message bref en haut de l'écran (une action a réussi), qui s'efface seul. */
@Composable
private fun Message(texte: String?, vu: () -> Unit, modifier: Modifier) {
    LaunchedEffect(texte) {
        if (texte != null) {
            delay(2800)
            vu()
        }
    }
    AnimatedVisibility(
        visible = texte != null,
        modifier = modifier,
        enter = slideInVertically(tween(260)) { -it } + fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
    ) {
        val c = Droplet.couleurs
        val forme = RoundedCornerShape(22.dp)
        Text(
            texte.orEmpty(),
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 52.dp, start = 20.dp, end = 20.dp)
                .shadow(16.dp, forme)
                .clip(forme)
                .background(c.surfaceElevee)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, fontWeight = FontWeight.Medium),
            color = c.texte,
        )
    }
}
