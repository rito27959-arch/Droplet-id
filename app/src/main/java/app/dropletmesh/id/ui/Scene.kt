// ============================================================================
// LES MOMENTS DE MARQUE — toujours sur le noir du logo, comme le site :
//   Ouverture     : une poussière d'argent converge et dessine le cœur, le vrai
//                   logo se pose, la ceinture se boucle (éclat + vibration),
//                   puis « DROPLET ID » monte lettre à lettre. Un toucher passe.
//   Presentation  : trois pages qui suivent le doigt (le logo tourne en 3D,
//                   les chiffres du code arrivent, les fils vers les apps se
//                   tracent), lumière qui glisse d'une page à l'autre.
// Si le téléphone demande moins d'animations, tout est montré à l'état final.
// ============================================================================
package app.dropletmesh.id.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.dropletmesh.id.R
import app.dropletmesh.id.designsystem.ArgentScene
import app.dropletmesh.id.designsystem.ArgentScene2
import app.dropletmesh.id.designsystem.GraphiteScene
import app.dropletmesh.id.designsystem.LogoCeinture
import app.dropletmesh.id.designsystem.Metal
import app.dropletmesh.id.designsystem.NoirScene
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.TitresDroplet
import app.dropletmesh.id.designsystem.cheminCoeur
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private fun borne(v: Float) = v.coerceIn(0f, 1f)
private fun plage(t: Float, a: Float, b: Float) = borne((t - a) / (b - a))
private fun doux(t: Float) = 1f - (1f - t).pow(3)

/** Le téléphone demande-t-il moins d'animations ? */
@Composable
fun animationsReduites(): Boolean {
    val ctx = LocalContext.current
    return remember { Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}

/** Le bouton principal des scènes : en métal, texte noir, comme sur le site. */
@Composable
fun BoutonMetal(texte: String, onClick: () -> Unit, modifier: Modifier = Modifier, actif: Boolean = true) {
    val retour = LocalHapticFeedback.current
    Box(
        modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(999.dp))
            .background(if (actif) Metal else Brush.verticalGradient(listOf(GraphiteScene, GraphiteScene)))
            .clickable(enabled = actif, role = Role.Button) { retour.performHapticFeedback(HapticFeedbackType.ContextClick); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(texte, style = TextStyle(fontFamily = TexteDroplet, fontSize = 17.sp, fontWeight = FontWeight.SemiBold), color = if (actif) Color(0xFF0E0F12) else ArgentScene2)
    }
}

/** Le noir de la scène, avec une lumière qui peut glisser (0 = gauche, 1 = droite). */
@Composable
fun FondScene(lumiere: Float = 0.5f, intensite: Float = 1f, modifier: Modifier = Modifier, contenu: @Composable BoxScope.() -> Unit) {
    Box(
        modifier.fillMaxSize().background(NoirScene).background(
            Brush.radialGradient(
                listOf(Color(0xFFC8CCD4).copy(alpha = 0.13f * intensite), Color.Transparent),
                center = Offset.Unspecified, radius = 1400f,
            ),
        ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFFC8CCD4).copy(alpha = 0.10f * intensite), Color.Transparent), Offset(size.width * (0.2f + lumiere * 0.6f), size.height * 0.34f), size.minDimension * 0.9f),
                size.minDimension * 0.9f, Offset(size.width * (0.2f + lumiere * 0.6f), size.height * 0.34f),
            )
        }
        contenu()
    }
}

// ----------------------------------------------------------------------------
// L'OUVERTURE
// ----------------------------------------------------------------------------

private class Grain(val tx: Float, val ty: Float, val sx: Float, val sy: Float, val sz: Float, val d: Float, val f: Float)

/** Les grains : des cibles tirées dans le cœur (512 × 512), des départs sur une sphère. */
private fun grains(n: Int): List<Grain> {
    val chemin = cheminCoeur().asAndroidPath()
    val region = android.graphics.Region().apply { setPath(chemin, android.graphics.Region(0, 0, 512, 512)) }
    val alea = Random(42)
    val cibles = ArrayList<Pair<Float, Float>>()
    var y = 140f
    while (y < 384f) { var x = 118f; while (x < 394f) { if (region.contains(x.toInt(), y.toInt())) cibles += x to y; x += 6.4f }; y += 6.4f }
    cibles.shuffle(alea)
    return cibles.take(n).map { (x, yy) ->
        val th = alea.nextFloat() * 2 * PI.toFloat(); val ph = acos(2 * alea.nextFloat() - 1); val r = 380f + alea.nextFloat() * 520f
        Grain(x, yy, 256 + r * sin(ph) * cos(th), 262 + r * sin(ph) * sin(th) * 0.8f, r * cos(ph) / 512f, alea.nextFloat() * 0.35f, alea.nextFloat() * 6.28f)
    }
}

@Composable
fun Ouverture(onFin: () -> Unit, figer: Float? = null) {
    val reduit = animationsReduites() && figer == null
    val temps = remember { Animatable(figer ?: if (reduit) 3.2f else 0f) }
    val retour = LocalHapticFeedback.current
    val poussiere = remember { grains(900) }
    var clic by remember { mutableStateOf(false) }
    val eclat = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (figer != null) return@LaunchedEffect // captures d'écran : l'instant est figé
        if (!reduit) temps.animateTo(3.3f, tween(3300, easing = LinearEasing)) else kotlinx.coroutines.delay(500)
        onFin()
    }
    val t = temps.value
    val boucle = doux(plage(t, 1.35f, 1.8f))
    LaunchedEffect(boucle >= 1f) {
        if (boucle >= 1f && !clic && !reduit && figer == null) { clic = true; retour.performHapticFeedback(HapticFeedbackType.Confirm); eclat.animateTo(1f, tween(650)) }
    }

    FondScene(intensite = 0.4f + 0.6f * plage(t, 0.2f, 1.2f), modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClickLabel = "Passer") { onFin() }) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.fillMaxWidth(0.62f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                // 1. La poussière converge et dessine le cœur.
                val conv = plage(t, 0f, 1.15f)
                val fondu = 1f - plage(t, 1.0f, 1.3f)
                if (fondu > 0f) Canvas(Modifier.fillMaxSize()) {
                    val k = size.minDimension / 512f
                    for (g in poussiere) {
                        val e = doux(plage(conv, g.d, g.d + 0.65f))
                        var x = g.sx + (g.tx - g.sx) * e - 256f; val yy = g.sy + (g.ty - g.sy) * e - 262f; var z = g.sz * (1 - e)
                        val a = (1 - e) * 1.4f; val ca = cos(a); val sa = sin(a)
                        val nx = x * ca + z * 512f * sa; z = (-x * sa / 512f + z * ca); x = nx
                        val persp = 1f / (1f + z.coerceIn(-1.5f, 3f) * 0.35f)
                        val px = (256f + x * persp) * k; val py = (262f + yy * persp) * k
                        val alpha = borne(persp * (0.25f + 0.75f * e) * fondu)
                        drawCircle(if (e > 0.95f) Color.White else ArgentScene, (1.1f + 1.3f * persp) * (1 - e * 0.3f) * k * 2.2f, Offset(px, py), alpha)
                    }
                }
                // 2. Le vrai logo se pose ; 3. la ceinture se boucle.
                val pose = doux(plage(t, 1.05f, 1.35f))
                if (pose > 0f) Box(
                    Modifier.fillMaxSize().graphicsLayer { alpha = pose; scaleX = 0.94f + pose * 0.06f; scaleY = scaleX }.clip(RoundedCornerShape(30)),
                ) { LogoCeinture(Modifier.fillMaxSize(), boucle = boucle, eclat = eclat.value) }
            }
            Spacer(Modifier.height(36.dp))
            // 4. « DROPLET ID » monte lettre à lettre.
            Row(Modifier.semantics { heading() }) {
                "DROPLET ID".forEachIndexed { i, l ->
                    val q = doux(plage(t, 1.9f + i * 0.05f, 2.35f + i * 0.05f))
                    Text(
                        if (l == ' ') " " else l.toString(),
                        Modifier.graphicsLayer { alpha = q; translationY = (1 - q) * 60f; rotationZ = (1 - q) * 8f },
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em),
                        color = ArgentScene,
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// LA PRÉSENTATION
// ----------------------------------------------------------------------------

private data class PagePresentation(val titre: String, val texte: String)

private val pagesPresentation = listOf(
    PagePresentation("Votre identité.\nBouclée.", "Une seule identité pour Droplet Messenger, Droplet Drop et Kunfox. Elle vit dans ce téléphone, et nulle part ailleurs."),
    PagePresentation("Sans mot de passe.\nSans e-mail.", "Pour vous connecter, vous vérifiez un nombre et vous approuvez d'un geste. Rien à retenir, rien à voler."),
    PagePresentation("Un identifiant\npar app.", "Chaque app reçoit un identifiant qui ne vaut que pour elle. Vous retirez un accès quand vous voulez."),
)

@Composable
fun Presentation(onTerminer: () -> Unit) {
    val etat = rememberPagerState { pagesPresentation.size }
    val portee = rememberCoroutineScope()
    val reduit = animationsReduites()
    val position = etat.currentPage + etat.currentPageOffsetFraction
    FondScene(lumiere = position / 2f) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    "Passer",
                    Modifier.clip(RoundedCornerShape(999.dp)).clickable(role = Role.Button) { onTerminer() }.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, fontWeight = FontWeight.Medium), color = ArgentScene2,
                )
            }
            HorizontalPager(etat, Modifier.weight(1f).fillMaxWidth()) { i ->
                val ecart = (etat.currentPage - i) + etat.currentPageOffsetFraction // 0 = page au centre
                val presence = borne(1f - abs(ecart))
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.fillMaxWidth().aspectRatio(1.15f).graphicsLayer {
                            rotationY = ecart * -28f; cameraDistance = 14f * density; translationX = ecart * 160f; alpha = 0.3f + 0.7f * presence
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (i) {
                            0 -> IllustrationLogo(etat, reduit)
                            1 -> IllustrationCode(presence, reduit)
                            else -> IllustrationApps(presence, reduit)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    val p = pagesPresentation[i]
                    Text(
                        p.titre,
                        Modifier.semantics { heading() }.graphicsLayer { translationX = ecart * 60f; alpha = presence },
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 32.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em, textAlign = TextAlign.Center),
                        color = Color.White,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        p.texte,
                        Modifier.graphicsLayer { translationX = ecart * 110f; alpha = presence },
                        style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center),
                        color = ArgentScene2,
                    )
                }
            }
            Indicateur(position, pagesPresentation.size)
            val derniere = etat.currentPage == pagesPresentation.lastIndex
            BoutonMetal(
                if (derniere) "Commencer" else "Continuer",
                onClick = { if (derniere) onTerminer() else portee.launch { etat.animateScrollToPage(etat.currentPage + 1) } },
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
            )
        }
    }
}

/** Des traits qui s'allongent : la page courante s'étire, en métal. */
@Composable
private fun Indicateur(position: Float, n: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalArrangement = Arrangement.Center) {
        repeat(n) { i ->
            val proche = borne(1f - abs(position - i))
            Box(
                Modifier.padding(horizontal = 4.dp).height(6.dp).width((6 + 22 * proche).dp).clip(CircleShape)
                    .background(if (proche > 0.5f) Metal else Brush.verticalGradient(listOf(Color(0xFF3A3C41), Color(0xFF3A3C41)))),
            )
        }
    }
}

/** Page 1 : le logo ; la ceinture se boucle quand la page s'installe. */
@Composable
private fun IllustrationLogo(etat: PagerState, reduit: Boolean) {
    val retour = LocalHapticFeedback.current
    val boucle = remember { Animatable(if (reduit) 1f else 0f) }
    val eclat = remember { Animatable(0f) }
    LaunchedEffect(etat.settledPage) {
        if (etat.settledPage == 0 && !reduit) {
            boucle.snapTo(0f); eclat.snapTo(0f)
            kotlinx.coroutines.delay(350)
            boucle.animateTo(1f, tween(650))
            retour.performHapticFeedback(HapticFeedbackType.Confirm)
            eclat.animateTo(1f, tween(650))
        }
    }
    Box(Modifier.fillMaxWidth(0.62f).aspectRatio(1f).clip(RoundedCornerShape(30))) {
        LogoCeinture(Modifier.fillMaxSize(), boucle = boucle.value, eclat = eclat.value)
    }
}

/** Page 2 : le nombre à vérifier ; ses chiffres arrivent un à un. */
@Composable
private fun IllustrationCode(presence: Float, reduit: Boolean) {
    val arrivee by animateFloatAsState(if (presence > 0.6f || reduit) 1f else 0f, tween(900), label = "code")
    Column(
        Modifier.fillMaxWidth(0.86f).clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1D1E23), Color(0xFF111215))))
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(R.drawable.app_kunfox), null, Modifier.size(52.dp).clip(RoundedCornerShape(13.dp)))
        Spacer(Modifier.height(12.dp))
        Text("Se connecter à Kunfox ?", style = TextStyle(fontFamily = TitresDroplet, fontSize = 18.sp, fontWeight = FontWeight.Bold), color = Color.White)
        Text("kunfox.app", style = TextStyle(fontFamily = TexteDroplet, fontSize = 13.sp), color = ArgentScene2)
        Spacer(Modifier.height(18.dp))
        Row {
            "482 913".forEachIndexed { i, ch ->
                val q = doux(plage(arrivee, i * 0.09f, 0.4f + i * 0.09f))
                Text(
                    ch.toString(),
                    Modifier.graphicsLayer { alpha = q; translationY = (1 - q) * 28f },
                    style = TextStyle(fontFamily = TitresDroplet, fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.04.em, fontFeatureSettings = "tnum"),
                    color = Color.White,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Le nombre affiché sur kunfox.app", style = TextStyle(fontFamily = TexteDroplet, fontSize = 12.sp), color = ArgentScene2)
    }
}

/** Page 3 : un fil part du logo vers chaque app et se trace. */
@Composable
private fun IllustrationApps(presence: Float, reduit: Boolean) {
    val trace by animateFloatAsState(if (presence > 0.6f || reduit) 1f else 0f, tween(1100), label = "fils")
    val apps = listOf(R.drawable.app_messenger, R.drawable.app_drop, R.drawable.app_kunfox)
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val haut = Offset(size.width / 2, size.height * 0.24f)
            listOf(0.16f, 0.5f, 0.84f).forEachIndexed { i, fx ->
                val bas = Offset(size.width * fx, size.height * 0.74f)
                val q = plage(trace, i * 0.12f, 0.6f + i * 0.12f)
                val fin = Offset(haut.x + (bas.x - haut.x) * q, haut.y + (bas.y - haut.y) * q)
                drawLine(ArgentScene.copy(alpha = 0.5f), haut, fin, strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Box(Modifier.align(Alignment.TopCenter).offset(y = 0.dp).size(92.dp).clip(RoundedCornerShape(30))) {
            LogoCeinture(Modifier.fillMaxSize())
        }
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            apps.forEachIndexed { i, res ->
                val q = doux(plage(trace, 0.45f + i * 0.12f, 0.85f + i * 0.12f))
                Image(painterResource(res), null, Modifier.size(58.dp).graphicsLayer { alpha = q; scaleX = 0.7f + 0.3f * q; scaleY = scaleX }.clip(RoundedCornerShape(15.dp)))
            }
        }
    }
}

