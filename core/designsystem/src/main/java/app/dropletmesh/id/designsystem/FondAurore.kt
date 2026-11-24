// ============================================================================
// LE FOND AURORE — ce que le verre de Glasmio réfracte.
// Quelques halos de couleur très doux (laiton, indigo, bleu, menthe) sur le
// fond blanc cassé ou noir profond, qui dérivent lentement (un cycle de
// 40 s). Sans lui, le verre n'a rien à montrer et l'app ressemble à une
// page de réglages. « Supprimer les animations » : les halos restent fixes.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private data class Halo(val x: Float, val y: Float, val rayon: Float, val clair: Color, val sombre: Color, val phase: Float)

private val halos = listOf(
    Halo(0.10f, 0.02f, 0.85f, Color(0x66E9C77B), Color(0x55B8913E), 0.0f), // laiton, en haut à gauche
    Halo(0.95f, 0.18f, 0.80f, Color(0x558E8CF5), Color(0x664B47C9), 1.7f), // indigo, en haut à droite
    Halo(0.20f, 0.70f, 0.75f, Color(0x4D7CC4F2), Color(0x55246B9E), 3.1f), // bleu
    Halo(0.90f, 0.95f, 0.80f, Color(0x4D8FE3C3), Color(0x4D1E7A5A), 4.4f), // menthe, en bas
)

@Composable
fun FondAurore(modifier: Modifier = Modifier) {
    val c = Droplet.couleurs
    val t by rememberInfiniteTransition(label = "aurore").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing), RepeatMode.Restart),
        label = "derive",
    )
    Canvas(modifier.clearAndSetSemantics { }) {
        drawRect(c.fond)
        val d = max(size.width, size.height)
        halos.forEach { h ->
            val a = 2 * PI.toFloat() * t + h.phase
            val centre = Offset(
                size.width * h.x + d * 0.06f * cos(a),
                size.height * h.y + d * 0.05f * sin(a * 0.8f),
            )
            val couleur = if (c.sombre) h.sombre else h.clair
            drawCircle(
                Brush.radialGradient(listOf(couleur, couleur.copy(alpha = 0f)), centre, d * h.rayon * 0.6f),
                radius = d * h.rayon * 0.6f,
                center = centre,
            )
        }
    }
}
