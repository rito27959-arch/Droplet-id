// ============================================================================
// LE LOGO, DESSINÉ EN COMPOSE — le cœur d'argent et sa ceinture de sécurité,
// en pièces pour pouvoir le boucler à l'écran (comme sur le site).
//
//   boucle : 0 = ceinture ouverte (la plaque est tirée en arrière),
//            1 = bouclée (la plaque est dans le boîtier).
//   eclat  : 0 → 1 joue l'éclat du « clic » (un halo blanc qui s'ouvre).
// Les coordonnées sont celles du SVG du site (512 × 512).
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

const val CHEMIN_COEUR = "M256 380 C 152 316 122 262 122 220 C 122 176 156 146 196 146 C 225 146 246 163 256 184 C 266 163 287 146 316 146 C 356 146 390 176 390 220 C 390 262 360 316 256 380 Z"

/** Le cœur, en coordonnées 512 × 512. */
fun cheminCoeur(): Path = PathParser().parsePathString(CHEMIN_COEUR).toPath()

private val ArgentCoeur = listOf(0f to Color(0xFFF5F6F8), 0.55f to Color(0xFFC9CCD1), 1f to Color(0xFF8B9097))
private val MetalPlaque = listOf(0f to Color.White, 0.5f to Color(0xFFC3C6CC), 1f to Color(0xFF8E939A))
private val Sangle = Color(0xFF33363C)
private val SangleFil = Color(0xFF3D4047)

@Composable
fun LogoCeinture(
    modifier: Modifier = Modifier,
    boucle: Float = 1f,
    eclat: Float = 0f,
    fond: Boolean = true,
    description: String? = "Logo de Droplet ID : un cœur argenté tenu par une ceinture de sécurité",
) {
    val coeur = remember { cheminCoeur() }
    val coeurLarge = remember {
        cheminCoeur().apply {
            transform(Matrix().apply { translate(256f, 262f); scale(1.16f, 1.16f); translate(-256f, -262f) })
        }
    }
    val m = if (description != null) modifier.semantics { contentDescription = description } else modifier
    Canvas(m) {
        val k = size.minDimension / 512f
        scale(k, k, pivot = Offset.Zero) {
            if (fond) {
                drawRect(
                    Brush.radialGradient(listOf(Color(0xFF1B1C20), NoirScene), center = Offset(256f, 215f), radius = 360f),
                    size = Size(512f, 512f),
                )
            }
            drawPath(coeur, Brush.linearGradient(*ArgentCoeur.toTypedArray(), start = Offset(122f, 146f), end = Offset(390f, 380f)))
            dessinerCeinture(coeurLarge, boucle.coerceIn(0f, 1f), eclat.coerceIn(0f, 1f))
        }
    }
}

private fun DrawScope.dessinerCeinture(clip: Path, boucle: Float, eclat: Float) {
    val cy = 262f
    rotate(-30f, pivot = Offset(256f, 262f)) {
        // L'ancrage, fixe, qui part du boîtier.
        clipPath(clip) { sangle(386f, cy - 22f, 260f, 44f) }
        // La sangle et sa plaque : la partie qui glisse.
        translate(left = (1f - boucle) * -96f) {
            clipPath(clip) {
                sangle(-120f, cy - 27f, 352f, 54f)
                drawRect(Color(0xFF565A62), Offset(-120f, cy - 27f), Size(352f, 3f))
                drawRect(Color(0xFF1D1F23), Offset(-120f, cy + 24f), Size(352f, 3f))
            }
            drawRoundRect(metal(cy - 36f, cy + 36f), Offset(214f, cy - 36f), Size(62f, 72f), CornerRadius(12f))
            drawRoundRect(Color(0xFF2A2C31), Offset(226f, cy - 14f), Size(30f, 28f), CornerRadius(6f))
            drawRoundRect(metal(cy - 18f, cy + 18f), Offset(270f, cy - 18f), Size(34f, 36f), CornerRadius(4f))
        }
        // Le boîtier et son bouton rouge.
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF2A2C31), Color(0xFF0E0F12)), cy - 40f, cy + 40f), Offset(292f, cy - 40f), Size(104f, 80f), CornerRadius(22f))
        drawRoundRect(Color(0xFF9EA3AA), Offset(292f, cy - 40f), Size(104f, 80f), CornerRadius(22f), style = Stroke(3f))
        val pic = if (eclat > 0f) (1f - kotlin.math.abs(eclat * 2f - 0.6f).coerceAtMost(1f)) else 0f
        drawRoundRect(lerp(RougeBoucle, Color(0xFFFF8A80), pic), Offset(306f, cy - 20f), Size(34f, 40f), CornerRadius(9f))
        drawRoundRect(Color(0xFF4B4E55), Offset(352f, cy - 6f), Size(32f, 12f), CornerRadius(6f))
        // L'éclat du clic.
        if (eclat in 0.001f..0.999f) {
            val r = 70f * (0.4f + eclat * 1.2f)
            val a = (if (eclat < 0.25f) eclat / 0.25f else 1f - (eclat - 0.25f) / 0.75f) * 0.85f
            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = a), Color.Transparent), Offset(290f, cy), r), r, Offset(290f, cy))
        }
    }
}

private fun metal(haut: Float, bas: Float) = Brush.verticalGradient(*MetalPlaque.toTypedArray(), startY = haut, endY = bas)

/** Une sangle tissée : fond graphite et fils en diagonale. */
private fun DrawScope.sangle(x: Float, y: Float, l: Float, h: Float) {
    drawRect(Sangle, Offset(x, y), Size(l, h))
    clipRect(x, y, x + l, y + h) {
        var d = x - h
        while (d < x + l) {
            drawLine(SangleFil, Offset(d, y + h), Offset(d + h, y), strokeWidth = 3f)
            d += 7f
        }
    }
}
