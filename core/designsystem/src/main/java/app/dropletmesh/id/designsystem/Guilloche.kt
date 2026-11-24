// ============================================================================
// LE GUILLOCHÉ — les lignes fines entrelacées des billets et des passeports,
// sur la carte d'identité seulement. Le motif est CALCULÉ à partir de l'ID
// Droplet (empreinte SHA-256) : chaque carte a le sien, toujours le même.
//
// Ce n'est pas une sécurité (n'importe qui peut le recalculer), c'est une
// signature visuelle. Fixe (aucune animation), invisible pour TalkBack.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Une rosace : [lignes] courbes polaires fermées
 *   ρ(t) = base_j + amplitude · sin(lobes · t + phase_j) + ondulation · sin(battement · t)
 * centrées en ([centreX], [centreY]) en fraction de la carte.
 */
data class ParametresGuilloche(
    val lobes: Int,
    val battement: Int,
    val lignes: Int,
    val amplitude: Float,
    val ondulation: Float,
    val dephasage: Float,
    val centreX: Float,
    val centreY: Float,
) {
    companion object {
        fun depuis(idDroplet: String): ParametresGuilloche {
            val h = MessageDigest.getInstance("SHA-256").digest(idDroplet.toByteArray(Charsets.UTF_8))
            fun octet(i: Int) = h[i].toInt() and 0xFF
            fun fraction(i: Int) = octet(i) / 255f
            return ParametresGuilloche(
                lobes = 5 + octet(0) % 7, // 5 à 11
                battement = 13 + octet(1) % 11, // 13 à 23
                lignes = 14 + octet(2) % 9, // 14 à 22
                amplitude = 0.05f + 0.06f * fraction(3),
                ondulation = 0.008f + 0.012f * fraction(4),
                dephasage = 0.08f + 0.22f * fraction(5),
                centreX = 0.62f + 0.22f * fraction(6),
                centreY = 0.35f + 0.30f * fraction(7),
            )
        }
    }
}

/** Les points d'une ligne du motif, dans un repère où 1 = le plus petit côté. */
fun pointsGuilloche(p: ParametresGuilloche, ligne: Int, pas: Int = 360): List<Pair<Float, Float>> {
    val base = 0.18f + 0.62f * ligne / (p.lignes - 1).coerceAtLeast(1)
    val phase = ligne * p.dephasage
    return (0..pas).map { i ->
        val t = 2 * PI * i / pas
        val rho = base + p.amplitude * sin(p.lobes * t + phase) + p.ondulation * sin(p.battement * t)
        Pair((rho * cos(t)).toFloat(), (rho * sin(t)).toFloat())
    }
}

@Composable
fun Guilloche(idDroplet: String, couleur: Color, modifier: Modifier = Modifier) {
    val p = remember(idDroplet) { ParametresGuilloche.depuis(idDroplet) }
    val lignes = remember(p) { (0 until p.lignes).map { pointsGuilloche(p, it) } }
    Canvas(modifier.clearAndSetSemantics { }) {
        val echelle = min(size.width, size.height) * 0.9f
        val centre = Offset(size.width * p.centreX, size.height * p.centreY)
        val trait = Stroke(width = 0.5.dp.toPx())
        lignes.forEach { points ->
            val chemin = Path()
            points.forEachIndexed { i, (x, y) ->
                val px = centre.x + x * echelle
                val py = centre.y + y * echelle
                if (i == 0) chemin.moveTo(px, py) else chemin.lineTo(px, py)
            }
            chemin.close()
            drawPath(chemin, couleur, style = trait)
        }
    }
}
