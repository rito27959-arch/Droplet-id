// ============================================================================
// LA RACINE — thème, verre, et ce qu'on montre :
//   présentation (une seule fois) → bienvenue (activer) → les cinq onglets.
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
import app.dropletmesh.id.designsystem.FondAurore
import app.dropletmesh.id.designsystem.Onglets
import app.dropletmesh.id.designsystem.SansTexte
import app.dropletmesh.id.designsystem.ThemeDroplet
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost
import io.github.neoruaa.glasmio.ui.GlasmioNavigationBar
import io.github.neoruaa.glasmio.ui.GlasmioTabItem
import kotlinx.coroutines.delay

enum class Onglet(val titre: String) { Accueil("Accueil"), Identite("Identité"), Acces("Accès"), Securite("Sécurité"), Compte("Compte") }

/** Les écrans poussés au-dessus d'un onglet (niveau 2 ou 3). */
sealed interface Ecran {
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
    val onglet: (Onglet) -> Unit,
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
            sampledContent = { FondAurore(Modifier.fillMaxSize()) },
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
    var onglet by rememberSaveable { mutableStateOf(Onglet.Accueil) }
    val pile = remember { mutableStateListOf<Ecran>() }
    val nav = remember {
        Navigation(
            ouvrir = { pile.add(it) },
            retour = { if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex) },
            onglet = { pile.clear(); onglet = it },
        )
    }
    BackHandler(enabled = pile.isNotEmpty()) { nav.retour() }
    BackHandler(enabled = pile.isEmpty() && onglet != Onglet.Accueil) { onglet = Onglet.Accueil }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = onglet,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "onglet",
        ) { o ->
            when (o) {
                Onglet.Accueil -> Accueil(ui, nav, onRafraichir = etat::chargerConnexions)
                Onglet.Identite -> Identite(ui)
                Onglet.Acces -> Acces(ui, nav, onRafraichir = etat::chargerConnexions)
                Onglet.Securite -> Securite(ui, nav, onOublier = etat::oublierCeTelephone)
                Onglet.Compte -> Compte(ui, nav)
            }
        }
        val compact = remember { mutableFloatStateOf(0f) }
        GlasmioNavigationBar(
            items = listOf(
                GlasmioTabItem(Onglet.Accueil, Onglet.Accueil.titre, Onglets.accueil),
                GlasmioTabItem(Onglet.Identite, Onglet.Identite.titre, Onglets.identite),
                GlasmioTabItem(Onglet.Acces, Onglet.Acces.titre, Onglets.acces),
                GlasmioTabItem(Onglet.Securite, Onglet.Securite.titre, Onglets.securite),
                GlasmioTabItem(Onglet.Compte, Onglet.Compte.titre, Onglets.compte),
            ),
            selectedKey = onglet,
            onSelected = { pile.clear(); onglet = it },
            onExpand = {},
            compactProgress = compact,
            compactSize = 48.dp,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // Les écrans poussés glissent depuis la droite, par-dessus l'onglet.
        val dessus = pile.lastOrNull()
        AnimatedContent(
            targetState = dessus,
            transitionSpec = {
                if (targetState != null && (initialState == null || pile.size > 0)) {
                    slideInHorizontally(tween(320)) { it } togetherWith fadeOut(tween(200))
                } else {
                    fadeIn(tween(120)) togetherWith slideOutHorizontally(tween(280)) { it }
                }
            },
            label = "pile",
        ) { e ->
            when (e) {
                null -> Box(Modifier)
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
            style = TextStyle(fontFamily = SansTexte, fontSize = 15.sp, fontWeight = FontWeight.Medium),
            color = c.texte,
        )
    }
}
