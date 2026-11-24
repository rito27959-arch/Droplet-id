// ============================================================================
// LA CARTE D'IDENTITÉ DROPLET — l'objet signature de l'app.
// Format ISO/IEC 7810 ID-1 (ratio 1,586), noire dans les deux modes, filet
// et guilloché en laiton, identifiant en Source Code Pro.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousRoundedRectangle
import io.github.neoruaa.glasmio.icons.GlasmioIcon

/** « a1B2c3D4e5F6g7H8… » → « A1B2 C3D4 E5F6 » : lisible, comparable à voix haute. */
fun idAffiche(compteId: String?): String =
    compteId.orEmpty().filter { it.isLetterOrDigit() }.take(12).uppercase().chunked(4).joinToString(" ").ifEmpty { "—" }

@Composable
fun CarteIdentite(
    compteId: String?,
    ligneBas: String,
    ligneBasDroite: String,
    modifier: Modifier = Modifier,
    verifiee: Boolean = true,
) {
    val forme = ContinuousRoundedRectangle(20.dp)
    val id = idAffiche(compteId)
    // L'entrée : la carte se pose en ressort (une fois).
    val entree = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entree.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 220f)) }
    // Le reflet : une bande de lumière traverse la carte en 1,4 s, toutes les 7 s.
    val balayage by rememberInfiniteTransition(label = "reflet").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 7000
                0f at 0
                0f at 2600
                1f at 4000 using FastOutSlowInEasing
                1f at 7000
            },
        ),
        label = "balayage",
    )
    Box(
        modifier
            .graphicsLayer {
                val e = entree.value
                alpha = e.coerceIn(0f, 1f)
                scaleX = 0.92f + 0.08f * e
                scaleY = 0.92f + 0.08f * e
                translationY = (1f - e) * 40.dp.toPx()
                rotationX = (1f - e) * 12f
            }
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .shadow(12.dp, forme, ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.10f))
            .clip(forme)
            .background(NoirCarte)
            .border(0.75.dp, LaitonCarte.copy(alpha = 0.28f), forme)
            .semantics(mergeDescendants = true) { contentDescription = "Carte d'identité Droplet, identifiant $id" },
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to LaitonCarte.copy(alpha = 0.10f), 0.45f to LaitonCarte.copy(alpha = 0f)),
            ),
        )
        Guilloche(compteId.orEmpty(), LaitonCarte.copy(alpha = 0.30f), Modifier.fillMaxSize())
        // Le reflet : une lumière en diagonale, comme sur une carte en métal.
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    0f to Color.White.copy(alpha = 0.10f),
                    0.35f to Color.White.copy(alpha = 0.02f),
                    0.6f to Color.White.copy(alpha = 0f),
                ),
            ),
        )
        // La bande de lumière du reflet.
        Box(
            Modifier.fillMaxSize().drawWithContent {
                drawContent()
                val x = -size.width * 0.6f + balayage * size.width * 2.2f
                drawRect(
                    Brush.linearGradient(
                        0f to Color.White.copy(alpha = 0f),
                        0.5f to Color.White.copy(alpha = 0.16f),
                        1f to Color.White.copy(alpha = 0f),
                        start = Offset(x, 0f),
                        end = Offset(x + size.width * 0.35f, size.height),
                    ),
                )
            },
        )
        Box(Modifier.width(4.dp).fillMaxHeight().background(LaitonCarte))
        Column(
            Modifier.fillMaxSize().padding(start = 22.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "DROPLET ID",
                    style = TextStyle(fontFamily = SansTexte, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                    color = LaitonCarte,
                )
                Spacer(Modifier.weight(1f))
                if (verifiee) {
                    GlasmioIcon(Icone.VERIFIE, contentDescription = null, tint = LaitonCarte, size = 18.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Actif", style = TextStyle(fontFamily = SansTexte, fontSize = 12.sp, fontWeight = FontWeight.Medium), color = LaitonCarte)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Identifiant",
                    style = TextStyle(fontFamily = SansTexte, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFF9C9CA3),
                )
                Text(
                    id,
                    style = TextStyle(fontFamily = MonoIdentifiants, fontSize = 25.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp),
                    color = Color(0xFFF2F1EE),
                )
            }
            Row(Modifier.fillMaxWidth()) {
                Text(ligneBas, style = TextStyle(fontFamily = SansTexte, fontSize = 12.sp), color = Color(0xFF9C9CA3))
                Spacer(Modifier.weight(1f))
                Text(ligneBasDroite, style = TextStyle(fontFamily = SansTexte, fontSize = 12.sp), color = Color(0xFF9C9CA3))
            }
        }
    }
}
