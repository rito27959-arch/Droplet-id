// ============================================================================
// LA BIENVENUE — créer son identité. Sur le noir de la scène, la ceinture est
// ouverte ; quand on crée son Droplet ID, elle se boucle.
// L'ouverture et la présentation sont dans Scene.kt.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.designsystem.ArgentScene
import app.dropletmesh.id.designsystem.ArgentScene2
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.LogoCeinture
import app.dropletmesh.id.designsystem.RougeBoucle
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.TitresDroplet
import io.github.neoruaa.glasmio.icons.GlasmioIcon

@Composable
fun Bienvenue(ui: UiApp, onActiver: () -> Unit) {
    val reduit = animationsReduites()
    val boucle by animateFloatAsState(if (ui.activation || reduit) 1f else 0f, tween(700), label = "boucle")
    FondScene {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp)) {
            Spacer(Modifier.weight(0.5f))
            Box(Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.5f).aspectRatio(1f).clip(RoundedCornerShape(30))) {
                LogoCeinture(Modifier.fillMaxSize(), boucle = boucle)
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Créez votre identité.",
                Modifier.fillMaxWidth().semantics { heading() },
                style = TextStyle(fontFamily = TitresDroplet, fontSize = 32.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em, textAlign = TextAlign.Center),
                color = Color.White,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Votre téléphone crée une clé que personne d'autre ne possède, pas même Droplet.",
                Modifier.fillMaxWidth(),
                style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center),
                color = ArgentScene2,
            )
            Spacer(Modifier.height(28.dp))
            Garantie(Icone.PASSKEY, "Une clé créée ici, qui ne quitte jamais ce téléphone")
            Garantie(Icone.VISIBILITE, "Ni numéro, ni e-mail, ni nom")
            Garantie(Icone.APPS, "Un identifiant différent pour chaque app")
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(ui.erreurActivation != null, enter = fadeIn(), exit = fadeOut()) {
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.Top) {
                    GlasmioIcon(Icone.ALERTE, contentDescription = "Erreur", tint = RougeBoucle, size = 20.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(ui.erreurActivation.orEmpty(), style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, lineHeight = 21.sp), color = ArgentScene)
                }
            }
            BoutonMetal(
                if (ui.activation) "Création de votre clé…" else "Créer mon Droplet ID",
                onClick = onActiver,
                actif = !ui.activation,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun Garantie(icone: String, texte: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        GlasmioIcon(icone, contentDescription = null, tint = ArgentScene2, size = 22.dp)
        Spacer(Modifier.width(14.dp))
        Text(texte, style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.5.sp, lineHeight = 21.sp), color = ArgentScene)
    }
}
