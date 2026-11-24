// ============================================================================
// LES COMPOSANTS DROPLET ID — par-dessus Glasmio, jamais à sa place.
//
//   BoutonPrincipal  : le seul bouton plein d'un écran (encre)
//   BoutonTexte      : action secondaire ou destructive (texte seul)
//   Pastille         : l'icône d'une ligne de liste, dans un cercle neutre
//   Sceau            : le moment de validation (le cercle se trace, 600 ms)
//   TitreSection / PiedSection : en-tête et explication d'un groupe
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.neoruaa.glasmio.glass.GlassEmphasis
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.ui.GlasmioButton
import io.github.neoruaa.glasmio.ui.GlasmioButtonSize

/** Le bouton principal : plein, à l'encre, pleine largeur. Un seul par écran. */
@Composable
fun BoutonPrincipal(texte: String, onClick: () -> Unit, modifier: Modifier = Modifier, actif: Boolean = true) {
    val c = Droplet.couleurs
    GlasmioButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = actif,
        emphasis = GlassEmphasis.Prominent,
        size = GlasmioButtonSize.Large,
    ) {
        Text(texte, color = if (actif) c.surEncre else c.texteSecondaire, style = TextStyle(fontFamily = SansTexte, fontSize = 17.sp, fontWeight = FontWeight.SemiBold))
    }
}

/** Une action secondaire, en texte. Destructive : en rouge. */
@Composable
fun BoutonTexte(texte: String, onClick: () -> Unit, modifier: Modifier = Modifier, destructif: Boolean = false, actif: Boolean = true) {
    val c = Droplet.couleurs
    GlasmioButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = actif,
        emphasis = GlassEmphasis.Regular,
        size = GlasmioButtonSize.Large,
        destructive = destructif,
    ) {
        Text(texte, color = if (destructif) c.erreur else c.texte, style = TextStyle(fontFamily = SansTexte, fontSize = 17.sp, fontWeight = FontWeight.Medium))
    }
}

/**
 * L'icône d'une ligne : une tuile de couleur aux coins continus, glyphe blanc,
 * comme les Réglages d'iOS. [teinte] remplace la couleur du sujet pour dire
 * un état (rouge = action requise, vert = protégé).
 */
@Composable
fun Pastille(icone: String, teinte: Color? = null, taille: Dp = 30.dp) {
    val fond = teinte ?: couleurTuile(icone)
    Box(
        Modifier.size(taille).clip(com.kyant.capsule.ContinuousRoundedRectangle(taille * 0.27f))
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f).compositeOver(fond), fond))),
        contentAlignment = Alignment.Center,
    ) {
        GlasmioIcon(icone, contentDescription = null, tint = Color.White, size = taille * 0.62f)
    }
}

@Composable
fun TitreEcran(texte: String, modifier: Modifier = Modifier) {
    Text(texte, modifier, style = TextStyle(fontFamily = SerifTitres, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold), color = Droplet.couleurs.texte)
}

@Composable
fun TitreSection(texte: String, modifier: Modifier = Modifier) {
    Text(
        texte,
        modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
        style = TextStyle(fontFamily = SerifTitres, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
        color = Droplet.couleurs.texte,
    )
}

@Composable
fun PiedSection(texte: String, modifier: Modifier = Modifier) {
    Text(
        texte,
        modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp),
        style = TextStyle(fontFamily = SansTexte, fontSize = 14.sp, lineHeight = 20.sp),
        color = Droplet.couleurs.texteSecondaire,
    )
}

@Composable
fun Corps(texte: String, modifier: Modifier = Modifier, secondaire: Boolean = false) {
    val c = Droplet.couleurs
    Text(texte, modifier, style = TextStyle(fontFamily = SansTexte, fontSize = 16.sp, lineHeight = 24.sp), color = if (secondaire) c.texteSecondaire else c.texte)
}

/**
 * Le sceau : un cercle en laiton qui se trace, puis la coche, puis une
 * vibration de confirmation. Joué une fois, au moment où une identité est
 * créée ou une connexion approuvée.
 */
@Composable
fun Sceau(modifier: Modifier = Modifier, taille: Dp = 88.dp, couleur: Color = Droplet.couleurs.laiton) {
    val trace = remember { Animatable(0f) }
    val retour = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        trace.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        retour.performHapticFeedback(HapticFeedbackType.Confirm)
    }
    Canvas(modifier.size(taille).clearAndSetSemantics { }) {
        val epaisseur = size.minDimension * 0.045f
        val trait = Stroke(epaisseur, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val p = trace.value
        val cercle = (p / 0.7f).coerceIn(0f, 1f)
        val coche = ((p - 0.6f) / 0.4f).coerceIn(0f, 1f)
        val marge = epaisseur
        drawArc(couleur, -90f, 360f * cercle, false, Offset(marge, marge), Size(size.width - 2 * marge, size.height - 2 * marge), style = trait)
        if (coche > 0f) {
            val chemin = Path().apply {
                moveTo(size.width * 0.32f, size.height * 0.52f)
                lineTo(size.width * 0.45f, size.height * 0.65f)
                lineTo(size.width * 0.70f, size.height * 0.38f)
            }
            val mesure = PathMeasure().apply { setPath(chemin, false) }
            val partiel = Path()
            mesure.getSegment(0f, mesure.length * coche, partiel, true)
            drawPath(partiel, couleur, style = trait)
        }
    }
}
