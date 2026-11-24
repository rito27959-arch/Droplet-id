// ============================================================================
// LA PRÉSENTATION (trois pages, une seule fois) ET LA BIENVENUE (activer).
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.designsystem.BoutonPrincipal
import app.dropletmesh.id.designsystem.AvatarDroplet
import app.dropletmesh.id.designsystem.Corps
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.TitresDroplet
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.ui.GlasmioButton
import kotlinx.coroutines.launch

private data class PageDePresentation(val icone: String, val titre: String, val texte: String)

private val pages = listOf(
    PageDePresentation(
        Icone.IDENTITE,
        "Une identité pour tout Droplet",
        "Connectez-vous à Droplet Messenger, Droplet Drop, Kunfox et aux sites partenaires avec ce téléphone.",
    ),
    PageDePresentation(
        Icone.SCEAU,
        "Sans mot de passe, sans e-mail",
        "Une clé est créée sur ce téléphone. Pour vous connecter, vous approuvez d'un geste. Rien de personnel n'est demandé.",
    ),
    PageDePresentation(
        Icone.APPS,
        "Vous voyez chaque accès",
        "Chaque app reçoit un identifiant qui ne vaut que pour elle. Vous retirez un accès quand vous voulez.",
    ),
)

@Composable
fun Presentation(onTerminer: () -> Unit) {
    val c = Droplet.couleurs
    val etat = rememberPagerState { pages.size }
    val portee = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            GlasmioButton(onClick = onTerminer) {
                Text("Passer", color = c.texte, style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, fontWeight = FontWeight.Medium))
            }
        }
        HorizontalPager(etat, Modifier.weight(1f).fillMaxWidth()) { i ->
            val p = pages[i]
            Column(
                Modifier.fillMaxSize().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (i == 0) {
                    AvatarDroplet(taille = 120.dp)
                } else {
                    Box(
                        Modifier.size(120.dp).clip(RoundedCornerShape(28.dp)).background(c.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        GlasmioIcon(p.icone, contentDescription = null, tint = if (i == 1) c.goutte else c.texte, size = 56.dp)
                    }
                }
                Spacer(Modifier.height(36.dp))
                Text(
                    p.titre,
                    Modifier.semantics { heading() },
                    style = TextStyle(fontFamily = TitresDroplet, fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
                    color = c.texte,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    p.texte,
                    style = TextStyle(fontFamily = TexteDroplet, fontSize = 17.sp, lineHeight = 25.sp),
                    color = c.texteSecondaire,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                val largeur by animateDpAsState(if (etat.currentPage == i) 22.dp else 7.dp, tween(250), label = "point")
                Box(
                    Modifier.padding(horizontal = 4.dp).height(7.dp).width(largeur).clip(CircleShape)
                        .background(if (etat.currentPage == i) c.texte else c.bordure),
                )
            }
        }
        val derniere = etat.currentPage == pages.lastIndex
        BoutonPrincipal(
            if (derniere) "Commencer" else "Continuer",
            onClick = { if (derniere) onTerminer() else portee.launch { etat.animateScrollToPage(etat.currentPage + 1) } },
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp),
        )
    }
}

@Composable
fun Bienvenue(ui: UiApp, onActiver: () -> Unit) {
    val c = Droplet.couleurs
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            "Votre identité Droplet",
            Modifier.semantics { heading() },
            style = TextStyle(fontFamily = TitresDroplet, fontSize = 34.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.022).em),
            color = c.texte,
        )
        Spacer(Modifier.height(12.dp))
        Corps("Elle tient dans ce téléphone : une clé que personne d'autre ne possède, pas même Droplet.", secondaire = true)
        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.weight(0.6f))
        // La goutte et sa lueur : la signature de la famille Droplet.
        AvatarDroplet(Modifier.align(Alignment.CenterHorizontally), taille = 112.dp)
        Spacer(Modifier.height(28.dp))
        Garantie(Icone.PASSKEY, "Une clé créée ici, qui ne quitte jamais ce téléphone")
        Garantie(Icone.VISIBILITE, "Ni numéro, ni e-mail, ni nom")
        Garantie(Icone.APPS, "Un identifiant différent pour chaque app")
        Spacer(Modifier.weight(1f))
        AnimatedContent(ui.erreurActivation, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "erreur") { e ->
            if (e != null) {
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.Top) {
                    GlasmioIcon(Icone.ALERTE, contentDescription = "Erreur", tint = c.erreur, size = 20.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(e, style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, lineHeight = 21.sp), color = c.texte)
                }
            }
        }
        BoutonPrincipal(
            if (ui.activation) "Création de votre clé…" else "Créer mon Droplet ID",
            onClick = onActiver,
            actif = !ui.activation,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun Garantie(icone: String, texte: String) {
    val c = Droplet.couleurs
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        GlasmioIcon(icone, contentDescription = null, tint = c.texteSecondaire, size = 22.dp)
        Spacer(Modifier.width(14.dp))
        Text(texte, style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 22.sp), color = c.texte)
    }
}
