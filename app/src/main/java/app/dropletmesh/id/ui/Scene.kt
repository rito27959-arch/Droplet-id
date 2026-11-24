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
import androidx.compose.animation.core.animateFloat
import app.dropletmesh.id.designsystem.RougeBoucle
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.runtime.withFrameNanos
import androidx.compose.animation.core.spring
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
fun BoutonMetal(texte: String, onClick: () -> Unit, modifier: Modifier = Modifier, actif: Boolean = true, brille: Boolean = false) {
    val retour = LocalHapticFeedback.current
    // Un reflet passe sur le métal toutes les trois secondes environ.
    val reflet = if (brille && actif) {
        val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "reflet")
        transition.animateFloat(-0.4f, 1.8f, androidx.compose.animation.core.infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "reflet").value
    } else null
    Box(
        modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(999.dp))
            .background(if (actif) Metal else Brush.verticalGradient(listOf(GraphiteScene, GraphiteScene)))
            .drawWithContent {
                drawContent()
                if (reflet != null && reflet >= 0f && reflet <= 1.4f) {
                    val x = size.width * reflet
                    drawRect(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent), Offset(x - 90f, 0f), Offset(x + 90f, size.height)))
                }
            }
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
// LA PRÉSENTATION — le principe de l'onboarding de Telegram, réécrit pour
// Droplet ID (aucun code repris) :
//   - l'illustration est FIXE au-dessus des textes ; seuls les textes glissent ;
//   - l'illustration suit le doigt en continu : ses 700 grains d'argent passent
//     d'une forme à l'autre (cœur → empreinte → réseau), en s'envolant un peu
//     et en tournant pendant le trajet, dans le sens du geste ;
//   - quand la page se pose, une animation propre à la page se joue (la
//     ceinture se boucle, un balayage lit l'empreinte, les apps apparaissent) ;
//   - des traits de pagination qui s'étirent, un bouton en métal qui brille.
// ----------------------------------------------------------------------------

private data class PagePresentation(val titre: String, val texte: String)

private val pagesPresentation = listOf(
    PagePresentation("Votre identité.\nBouclée.", "Une seule identité pour Droplet Messenger, Droplet Drop et Kunfox. Elle vit dans ce téléphone, et nulle part ailleurs."),
    PagePresentation("Sans mot de passe.\nSans e-mail.", "Pour vous connecter, vous vérifiez un nombre et vous approuvez avec votre empreinte. Rien à retenir, rien à voler."),
    PagePresentation("Un identifiant\npar app.", "Chaque app reçoit un identifiant qui ne vaut que pour elle. Vous retirez un accès quand vous voulez."),
)

/** Les trois formes, en coordonnées 512 × 512 : n points chacune (x, y entrelacés). */
private fun formesPresentation(n: Int): List<FloatArray> {
    val coeur = ArrayList<Pair<Float, Float>>()
    val region = android.graphics.Region().apply { setPath(cheminCoeur().asAndroidPath(), android.graphics.Region(0, 0, 512, 512)) }
    var y = 140f
    while (y < 384f) { var x = 118f; while (x < 394f) { if (region.contains(x.toInt(), y.toInt())) coeur += x to y; x += 7f }; y += 7f }

    // L'empreinte : des boucles concentriques, ouvertes vers le bas.
    val empreinte = ArrayList<Pair<Float, Float>>()
    for (i in 0 until 8) {
        val r = 22f + i * 19f
        val (debut, fin) = if (i < 2) 0f to 360f else (150f - i * 2f) to (390f + i * 2f)
        val pas = 6.5f / r * (180f / PI.toFloat())
        var a = debut
        while (a <= fin) {
            val rad = a * PI.toFloat() / 180f
            empreinte += (256f + r * cos(rad)) to (262f + r * 1.22f * sin(rad))
            a += pas
        }
    }

    // Le réseau : un nœud central, trois nœuds, des fils.
    val reseau = ArrayList<Pair<Float, Float>>()
    fun anneau(cx: Float, cy: Float, r: Float) { var a = 0f; while (a < 360f) { val rad = a * PI.toFloat() / 180f; reseau += (cx + r * cos(rad)) to (cy + r * sin(rad)); a += 360f / (r * 0.9f) } }
    fun fil(x1: Float, y1: Float, x2: Float, y2: Float) { val l = kotlin.math.hypot(x2 - x1, y2 - y1); var d = 0f; while (d < l) { reseau += (x1 + (x2 - x1) * d / l) to (y1 + (y2 - y1) * d / l); d += 8f } }
    val noeuds = listOf(256f to 96f, 112f to 384f, 400f to 384f)
    anneau(256f, 262f, 46f); anneau(256f, 262f, 30f)
    for ((nx, ny) in noeuds) {
        anneau(nx, ny, 36f)
        val dx = nx - 256f; val dy = ny - 262f; val l = kotlin.math.hypot(dx, dy)
        fil(256f + dx / l * 52f, 262f + dy / l * 52f, nx - dx / l * 42f, ny - dy / l * 42f)
    }

    val alea = Random(7)
    return listOf(coeur, empreinte, reseau).map { points ->
        val melange = points.shuffled(alea)
        FloatArray(2 * n).also { out ->
            for (k in 0 until n) {
                val (px, py) = melange[k % melange.size]
                val bruit = if (k >= melange.size) 2.5f else 0f
                out[2 * k] = px + (alea.nextFloat() - 0.5f) * bruit
                out[2 * k + 1] = py + (alea.nextFloat() - 0.5f) * bruit
            }
        }
    }
}

/** Les nœuds du réseau, là où se posent les icônes des apps (512 × 512). */
private val noeudsApps = listOf(256f to 96f, 112f to 384f, 400f to 384f)

@Composable
fun Presentation(onTerminer: () -> Unit) {
    val etat = rememberPagerState { pagesPresentation.size }
    val portee = rememberCoroutineScope()
    val reduit = animationsReduites()
    val retour = LocalHapticFeedback.current
    val n = 700
    val formes = remember { formesPresentation(n) }
    val retards = remember { val r = Random(11); FloatArray(n) { r.nextFloat() } }

    // La position continue du pager, comme le « scroll offset » qui pilote l'illustration.
    val position = (etat.currentPage + etat.currentPageOffsetFraction).coerceIn(0f, (pagesPresentation.size - 1).toFloat())
    val posee = etat.settledPage
    val immobile = !etat.isScrollInProgress && abs(position - posee) < 0.01f

    // L'horloge des animations au repos (respiration des grains, balayage).
    var horloge by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reduit) {
        if (reduit) return@LaunchedEffect
        val debut = withFrameNanos { it }
        while (true) withFrameNanos { horloge = (it - debut) / 1e9f }
    }

    // Une petite impulsion quand une page se pose (vibration légère).
    val impulsion = remember { Animatable(1f) }
    LaunchedEffect(posee) {
        if (reduit) return@LaunchedEffect
        retour.performHapticFeedback(HapticFeedbackType.SegmentTick)
        impulsion.snapTo(0f); impulsion.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 260f))
    }

    // Page 1 : la ceinture se boucle une fois la page posée.
    val boucle = remember { Animatable(if (reduit) 1f else 0f) }
    val eclat = remember { Animatable(0f) }
    LaunchedEffect(posee) {
        if (posee == 0 && !reduit) {
            boucle.snapTo(0f); eclat.snapTo(0f)
            kotlinx.coroutines.delay(420)
            boucle.animateTo(1f, tween(620))
            retour.performHapticFeedback(HapticFeedbackType.Confirm)
            eclat.animateTo(1f, tween(650))
        }
    }
    // Page 2 : le balayage qui lit l'empreinte ; page 3 : les apps se posent.
    val lecture = remember { Animatable(0f) }
    val apps = remember { Animatable(if (reduit) 1f else 0f) }
    LaunchedEffect(posee) {
        if (reduit) return@LaunchedEffect
        if (posee == 1) { lecture.snapTo(0f); kotlinx.coroutines.delay(200); lecture.animateTo(1f, tween(1400, easing = LinearEasing)) }
        if (posee == 2) { apps.snapTo(0f); kotlinx.coroutines.delay(150); apps.animateTo(1f, tween(900)) }
    }

    val logoVisible by animateFloatAsState(if (immobile && posee == 0 || reduit && posee == 0) 1f else 0f, tween(if (immobile) 450 else 160), label = "logo")
    val appsVisibles = if (posee == 2 && immobile) apps.value else 0f

    FondScene(lumiere = position / 2f) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    "Passer",
                    Modifier.clip(RoundedCornerShape(999.dp)).clickable(role = Role.Button) { onTerminer() }.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, fontWeight = FontWeight.Medium), color = ArgentScene2,
                )
            }

            // L'ILLUSTRATION, FIXE : seuls les grains bougent.
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxWidth(0.8f).aspectRatio(1f).graphicsLayer {
                    val i = impulsion.value; scaleX = 0.96f + 0.04f * i; scaleY = scaleX
                }) {
                    Canvas(Modifier.fillMaxSize()) {
                        val k = size.minDimension / 512f
                        val i0 = position.toInt().coerceAtMost(pagesPresentation.size - 2)
                        val fr = position - i0
                        val a = formes[i0]; val b = formes[i0 + 1]
                        val sens = if (etat.currentPageOffsetFraction >= 0f) 1f else -1f
                        val voile = 1f - logoVisible * 0.92f
                        for (q in 0 until n) {
                            val d = retards[q]
                            val t = doux(plage(fr, d * 0.3f, d * 0.3f + 0.7f))
                            val vol = sin(PI.toFloat() * t)
                            var x = a[2 * q] + (b[2 * q] - a[2 * q]) * t - 256f
                            var y = a[2 * q + 1] + (b[2 * q + 1] - a[2 * q + 1]) * t - 262f
                            // L'envol : les grains s'écartent du centre au milieu du trajet…
                            val ecart = 1f + vol * (0.35f + d * 0.5f)
                            x *= ecart; y *= ecart
                            // … et tournent autour de l'axe vertical, dans le sens du geste.
                            val ang = vol * 1.1f * sens
                            var z = (d - 0.5f) * 140f * vol
                            val ca = cos(ang); val sa = sin(ang)
                            val nx = x * ca + z * sa; z = -x * sa + z * ca; x = nx
                            // La respiration au repos.
                            if (!reduit) { x += sin(horloge * 1.3f + d * 40f) * 1.6f; y += cos(horloge * 1.1f + d * 30f) * 1.6f }
                            val persp = 1f / (1f + (z / 512f).coerceIn(-0.6f, 2f))
                            val px = (256f + x * persp) * k; val py = (262f + y * persp) * k
                            // Le balayage de l'empreinte allume les grains qu'il traverse.
                            val ligne = 120f + lecture.value * 300f
                            val lu = if (posee == 1 && immobile && lecture.value in 0.001f..0.999f) borne(1f - abs(a[2 * q + 1] - ligne) / 26f) else 0f
                            val alpha = borne(persp * (0.55f + 0.45f * (1f - vol)) * voile + lu * 0.6f)
                            drawCircle(if (lu > 0.3f) Color.White else ArgentScene, (1.25f + 1.1f * persp + lu * 0.9f) * k * 2f, Offset(px, py), alpha)
                        }
                        if (posee == 1 && immobile && lecture.value in 0.001f..0.999f) {
                            val yl = (120f + lecture.value * 300f) * k
                            drawLine(
                                Brush.horizontalGradient(listOf(Color.Transparent, RougeBoucle, Color.Transparent), 60f * k, 452f * k),
                                Offset(60f * k, yl), Offset(452f * k, yl), strokeWidth = 2.dp.toPx(),
                            )
                        }
                    }
                    // Page 1 : le vrai logo se pose sur le cœur de grains, et se boucle.
                    if (logoVisible > 0f) {
                        Box(Modifier.fillMaxSize().graphicsLayer { alpha = logoVisible; scaleX = 0.92f + logoVisible * 0.08f; scaleY = scaleX }.clip(RoundedCornerShape(30))) {
                            LogoCeinture(Modifier.fillMaxSize(), boucle = boucle.value, eclat = eclat.value)
                        }
                    }
                    // Page 3 : les icônes des apps se posent sur les nœuds.
                    if (appsVisibles > 0f) {
                        val icones = listOf(R.drawable.app_messenger, R.drawable.app_drop, R.drawable.app_kunfox)
                        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
                            val unite = maxWidth / 512f
                            noeudsApps.forEachIndexed { j, (nx, ny) ->
                                val q = doux(plage(appsVisibles, j * 0.15f, 0.55f + j * 0.15f))
                                Image(
                                    painterResource(icones[j]), null,
                                    Modifier.offset(unite * (nx - 30f), unite * (ny - 30f)).size(unite * 60f)
                                        .graphicsLayer { alpha = q; scaleX = 0.6f + 0.4f * q; scaleY = scaleX }
                                        .clip(RoundedCornerShape(percent = 26)),
                                )
                            }
                        }
                    }
                }
            }

            // LES TEXTES, SEULS À GLISSER.
            HorizontalPager(etat, Modifier.fillMaxWidth().height(200.dp)) { i ->
                val ecart = (etat.currentPage - i) + etat.currentPageOffsetFraction
                val presence = borne(1f - abs(ecart) * 1.4f)
                Column(Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val p = pagesPresentation[i]
                    Text(
                        p.titre,
                        Modifier.semantics { heading() }.graphicsLayer { alpha = presence },
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 32.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em, textAlign = TextAlign.Center),
                        color = Color.White,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        p.texte,
                        Modifier.graphicsLayer { alpha = presence; translationX = ecart * 40f },
                        style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center),
                        color = ArgentScene2,
                    )
                }
            }
            Indicateur(position, pagesPresentation.size)
            val derniere = etat.currentPage == pagesPresentation.lastIndex
            BoutonMetal(
                if (derniere) "Commencer" else "Continuer",
                onClick = { if (derniere) onTerminer() else portee.launch { etat.animateScrollToPage(etat.currentPage + 1, animationSpec = tween(520)) } },
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
                brille = !reduit,
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
