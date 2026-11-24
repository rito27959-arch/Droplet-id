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
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
// LA PRÉSENTATION — la mise en scène de l'accueil de Telegram (même structure,
// même façon de bouger), avec les illustrations et le code de Droplet ID :
//   - une illustration au centre, au-dessus du texte ; à chaque page, l'ancienne
//     se rétracte en tournant, la nouvelle jaillit avec un léger rebond, et ses
//     éléments satellites sortent un à un ;
//   - pendant le glissement, l'illustration suit un peu le doigt ;
//   - le titre et le texte glissent avec le doigt ;
//   - des points dont l'indicateur s'étire de l'un à l'autre ;
//   - un seul bouton, le même sur toutes les pages, avec un reflet qui passe.
// ----------------------------------------------------------------------------

private data class PagePresentation(val titre: String, val texte: String)

private val pagesPresentation = listOf(
    PagePresentation("Droplet ID", "Une seule identité pour **Droplet Messenger**, **Droplet Drop** et **Kunfox**. Elle vit dans votre téléphone."),
    PagePresentation("Un geste", "Pour vous connecter, vous approuvez avec votre **empreinte**. C'est tout."),
    PagePresentation("Sans mot de passe", "Rien à retenir, **rien à voler**. Votre clé ne quitte jamais ce téléphone."),
    PagePresentation("Toutes vos apps", "Chaque app reçoit un **identifiant à elle**. Deux apps ne peuvent pas savoir que c'est vous."),
    PagePresentation("Privé", "Ni numéro, ni e-mail, ni nom. **Vous retirez un accès** quand vous voulez."),
)

/** « **gras** » → texte enrichi (comme les mots en gras des pages de Telegram). */
private fun enrichi(texte: String) = androidx.compose.ui.text.buildAnnotatedString {
    var gras = false
    texte.split("**").forEach { morceau ->
        if (gras) withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.White)) { append(morceau) } else append(morceau)
        gras = !gras
    }
}

@Composable
fun Presentation(onTerminer: () -> Unit) {
    val etat = rememberPagerState { pagesPresentation.size }
    val reduit = animationsReduites()
    val retour = LocalHapticFeedback.current
    val cible = etat.targetPage
    var precedente by remember { mutableStateOf(0) }
    val sens = if (cible >= precedente) 1f else -1f
    LaunchedEffect(cible) { if (cible != precedente && !reduit) retour.performHapticFeedback(HapticFeedbackType.SegmentTick); precedente = cible }
    val glisse = etat.currentPageOffsetFraction

    Box(Modifier.fillMaxSize().background(NoirScene)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Spacer(Modifier.weight(0.9f))
            // L'illustration : elle change quand la page cible change, et suit un peu le doigt.
            Box(
                Modifier.fillMaxWidth().height(240.dp).graphicsLayer {
                    translationX = -glisse * 40.dp.toPx()
                    val r = 1f - abs(glisse) * 0.12f; scaleX = r; scaleY = r
                },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = cible,
                    transitionSpec = {
                        if (reduit) {
                            androidx.compose.animation.fadeIn(tween(1)) togetherWith androidx.compose.animation.fadeOut(tween(1))
                        } else {
                            (androidx.compose.animation.scaleIn(spring(dampingRatio = 0.55f, stiffness = 320f), initialScale = 0.35f) + androidx.compose.animation.fadeIn(tween(180))) togetherWith
                                (androidx.compose.animation.scaleOut(tween(220), targetScale = 0.35f) + androidx.compose.animation.fadeOut(tween(200)))
                        }
                    },
                    label = "illustration",
                ) { page -> Illustration(page, sens, reduit) }
            }
            Spacer(Modifier.height(36.dp))
            // Le texte, qui glisse avec le doigt.
            HorizontalPager(etat, Modifier.fillMaxWidth().height(150.dp)) { i ->
                val p = pagesPresentation[i]
                Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        p.titre,
                        Modifier.semantics { heading() },
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.025).em, textAlign = TextAlign.Center),
                        color = Color.White,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        enrichi(p.texte),
                        style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center),
                        color = ArgentScene2,
                    )
                }
            }
            Points(etat.currentPage + glisse, pagesPresentation.size)
            Spacer(Modifier.weight(1f))
            BoutonMetal(
                "Commencer",
                onClick = onTerminer,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 20.dp),
                brille = !reduit,
            )
        }
    }
}

/** Les points : l'indicateur s'étire vers la page suivante, puis s'y rétracte. */
@Composable
private fun Points(position: Float, n: Int) {
    val pas = 14.dp; val d = 6.dp
    Box(Modifier.fillMaxWidth().padding(top = 18.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.width(pas * (n - 1) + d).height(d)) {
            val p = pas.toPx(); val r = d.toPx() / 2
            repeat(n) { drawCircle(Color(0xFF3A3C41), r, Offset(r + it * p, r)) }
            val i = position.toInt().coerceIn(0, n - 1); val f = (position - i).coerceIn(0f, 1f)
            val debut = r + i * p + p * borne((f - 0.5f) * 2f)
            val fin = r + i * p + p * borne(f * 2f)
            drawLine(ArgentScene, Offset(debut, r), Offset(fin, r), strokeWidth = r * 2, cap = StrokeCap.Round)
        }
    }
}

// --- Les illustrations -------------------------------------------------------

private val Encre = Color(0xFF0E0F12)

/** L'entrée commune : l'illustration arrive en tournant dans le sens du geste. */
@Composable
private fun Entree(sens: Float, reduit: Boolean, contenu: @Composable BoxScope.(Float) -> Unit) {
    val t = remember { Animatable(if (reduit) 1f else 0f) }
    LaunchedEffect(Unit) { if (!reduit) t.animateTo(1f, tween(900, easing = LinearEasing)) }
    Box(
        Modifier.size(240.dp).graphicsLayer { rotationZ = (1f - doux(plage(t.value, 0f, 0.4f))) * 24f * sens },
        contentAlignment = Alignment.Center,
    ) { contenu(t.value) }
}

@Composable
private fun Illustration(page: Int, sens: Float, reduit: Boolean) = Entree(sens, reduit) { t ->
    when (page) {
        0 -> IllustrationIdentite(t, reduit)
        1 -> IllustrationGeste(t, reduit)
        2 -> IllustrationSansMotDePasse(t)
        3 -> IllustrationApps(t)
        else -> IllustrationPrive(t)
    }
}

/** Un satellite qui sort du centre vers sa place, avec un rebond (retard en fraction de t). */
@Composable
private fun BoxScope.Satellite(t: Float, retard: Float, angle: Float, rayon: Dp, taille: Dp, contenu: @Composable () -> Unit) {
    val q = plage(t, retard, retard + 0.35f)
    val rebond = if (q <= 0f) 0f else 1f + sin(q * PI.toFloat()) * 0.18f * (1f - q) - (1f - doux(q))
    val rad = angle * PI.toFloat() / 180f
    Box(
        Modifier.align(Alignment.Center).size(taille).graphicsLayer {
            val dist = rayon.toPx() * doux(q)
            translationX = cos(rad) * dist; translationY = sin(rad) * dist
            scaleX = rebond.coerceAtLeast(0f); scaleY = scaleX; alpha = borne(q * 3f)
            rotationZ = (1f - doux(q)) * -90f
        },
        contentAlignment = Alignment.Center,
    ) { contenu() }
}

/** Le disque de métal au centre, avec son glyphe sombre. */
@Composable
private fun Disque(taille: Dp = 118.dp, glyphe: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    Canvas(Modifier.size(taille)) {
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent), center, size.minDimension * 0.9f), size.minDimension * 0.9f)
        drawCircle(Metal, size.minDimension / 2)
        drawCircle(Color.White.copy(alpha = 0.5f), size.minDimension / 2 - 1.dp.toPx(), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
        glyphe()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.trait() = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)

/** Une petite étoile à quatre branches (scintillement). */
@Composable
private fun Etincelle(taille: Dp, eclat: Float) {
    Canvas(Modifier.size(taille)) {
        val c = center; val r = size.minDimension / 2
        val chemin = androidx.compose.ui.graphics.Path().apply {
            moveTo(c.x, c.y - r); quadraticTo(c.x, c.y, c.x + r, c.y); quadraticTo(c.x, c.y, c.x, c.y + r)
            quadraticTo(c.x, c.y, c.x - r, c.y); quadraticTo(c.x, c.y, c.x, c.y - r); close()
        }
        drawPath(chemin, ArgentScene.copy(alpha = 0.4f + 0.6f * eclat))
    }
}

@Composable
private fun scintillement(reduit: Boolean, decalage: Float): Float {
    if (reduit) return 1f
    val tr = androidx.compose.animation.core.rememberInfiniteTransition(label = "scintille")
    return tr.animateFloat(0f, 1f, androidx.compose.animation.core.infiniteRepeatable(tween(1600, delayMillis = (decalage * 700).toInt()), androidx.compose.animation.core.RepeatMode.Reverse), label = "s").value
}

/** 1. Le logo ; la ceinture se boucle, des étincelles l'entourent. */
@Composable
private fun BoxScope.IllustrationIdentite(t: Float, reduit: Boolean) {
    val retour = LocalHapticFeedback.current
    val boucle = remember { Animatable(if (reduit) 1f else 0f) }
    val eclat = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (reduit) return@LaunchedEffect
        kotlinx.coroutines.delay(380); boucle.animateTo(1f, tween(520))
        retour.performHapticFeedback(HapticFeedbackType.Confirm); eclat.animateTo(1f, tween(650))
    }
    listOf(-150f to 0.10f, -40f to 0.18f, 20f to 0.26f, 130f to 0.34f, 200f to 0.42f).forEachIndexed { i, (a, r) ->
        Satellite(t, r, a, 104.dp, if (i % 2 == 0) 14.dp else 10.dp) { Etincelle(if (i % 2 == 0) 14.dp else 10.dp, scintillement(reduit, i * 0.3f)) }
    }
    Box(Modifier.align(Alignment.Center).size(136.dp).clip(RoundedCornerShape(30))) {
        LogoCeinture(Modifier.fillMaxSize(), boucle = boucle.value, eclat = eclat.value)
    }
}

/** 2. L'empreinte, et des ondes qui s'en échappent. */
@Composable
private fun BoxScope.IllustrationGeste(t: Float, reduit: Boolean) {
    val onde = if (reduit) 0f else {
        val tr = androidx.compose.animation.core.rememberInfiniteTransition(label = "onde")
        tr.animateFloat(0f, 1f, androidx.compose.animation.core.infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "o").value
    }
    Canvas(Modifier.align(Alignment.Center).size(240.dp)) {
        repeat(3) { k ->
            val q = (onde + k / 3f) % 1f
            drawCircle(ArgentScene.copy(alpha = (1f - q) * 0.35f * borne(t * 3f)), size.minDimension * (0.25f + q * 0.25f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
        }
    }
    Disque {
        val s = trait(); val c = center; val u = size.minDimension
        for (i in 0 until 5) {
            val r = u * (0.08f + i * 0.065f)
            val debut = if (i == 0) 0f else 200f - i * 6f
            val balayage = if (i == 0) 360f else 140f + i * 12f
            drawArc(Encre, debut, balayage * plage(t, 0.15f + i * 0.06f, 0.5f + i * 0.06f), false, Offset(c.x - r, c.y - r * 1.15f), androidx.compose.ui.geometry.Size(r * 2, r * 2.3f), style = s)
        }
    }
}

/** 3. Les points d'un mot de passe apparaissent, puis un trait les barre. */
@Composable
private fun BoxScope.IllustrationSansMotDePasse(t: Float) {
    Disque {
        val s = trait(); val u = size.minDimension; val c = center
        // une clé
        drawCircle(Encre, u * 0.11f, Offset(c.x - u * 0.14f, c.y), style = s)
        drawLine(Encre, Offset(c.x - u * 0.03f, c.y), Offset(c.x + u * 0.24f, c.y), s.width, StrokeCap.Round)
        drawLine(Encre, Offset(c.x + u * 0.14f, c.y), Offset(c.x + u * 0.14f, c.y + u * 0.09f), s.width, StrokeCap.Round)
        drawLine(Encre, Offset(c.x + u * 0.22f, c.y), Offset(c.x + u * 0.22f, c.y + u * 0.07f), s.width, StrokeCap.Round)
    }
    // ●●●●●● au-dessus, barrés
    Row(Modifier.align(Alignment.TopCenter).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        repeat(6) { i ->
            val q = doux(plage(t, 0.2f + i * 0.05f, 0.4f + i * 0.05f))
            Box(Modifier.size(10.dp).graphicsLayer { scaleX = q; scaleY = q }.clip(CircleShape).background(ArgentScene))
        }
    }
    Canvas(Modifier.align(Alignment.TopCenter).padding(top = 8.dp).width(118.dp).height(10.dp)) {
        val q = doux(plage(t, 0.62f, 0.85f))
        drawLine(RougeBoucle, Offset(-6.dp.toPx(), size.height / 2), Offset(-6.dp.toPx() + (size.width + 12.dp.toPx()) * q, size.height / 2), 2.5.dp.toPx(), StrokeCap.Round)
    }
}

/** 4. Le logo au centre ; les apps en jaillissent, chacune à sa place. */
@Composable
private fun BoxScope.IllustrationApps(t: Float) {
    val icones = listOf(R.drawable.app_messenger, R.drawable.app_drop, R.drawable.app_kunfox)
    listOf(-90f, 30f, 150f).forEachIndexed { i, a ->
        Satellite(t, 0.12f + i * 0.1f, a, 92.dp, 54.dp) {
            Image(painterResource(icones[i]), null, Modifier.fillMaxSize().clip(RoundedCornerShape(percent = 26)))
        }
    }
    listOf(-30f, 90f, 210f).forEachIndexed { i, a ->
        Satellite(t, 0.42f + i * 0.08f, a, 98.dp, 8.dp) { Box(Modifier.fillMaxSize().clip(CircleShape).background(ArgentScene2)) }
    }
    Box(Modifier.align(Alignment.Center).size(96.dp).clip(RoundedCornerShape(30))) { LogoCeinture(Modifier.fillMaxSize(), description = null) }
}

/** 5. Le cadenas se ferme (son anse descend), puis un sceau se pose. */
@Composable
private fun BoxScope.IllustrationPrive(t: Float) {
    Disque {
        val s = trait(); val u = size.minDimension; val c = center
        val ferme = doux(plage(t, 0.25f, 0.55f))
        val montee = (1f - ferme) * u * 0.08f
        drawArc(Encre, 180f, 180f, false, Offset(c.x - u * 0.12f, c.y - u * 0.25f - montee), androidx.compose.ui.geometry.Size(u * 0.24f, u * 0.26f), style = s)
        drawLine(Encre, Offset(c.x + u * 0.12f, c.y - u * 0.12f - montee), Offset(c.x + u * 0.12f, c.y - u * 0.04f), s.width, StrokeCap.Round)
        drawRoundRect(Encre, Offset(c.x - u * 0.2f, c.y - u * 0.05f), androidx.compose.ui.geometry.Size(u * 0.4f, u * 0.3f), androidx.compose.ui.geometry.CornerRadius(u * 0.06f))
        drawCircle(ArgentScene, u * 0.035f, Offset(c.x, c.y + u * 0.09f))
    }
    Satellite(t, 0.55f, 45f, 52.dp, 40.dp) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.verticalGradient(listOf(Color(0xFF2A2C31), Encre)), size.minDimension / 2)
            drawCircle(ArgentScene, size.minDimension / 2 - 1.dp.toPx(), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
            val c = center; val u = size.minDimension
            val chemin = androidx.compose.ui.graphics.Path().apply { moveTo(c.x - u * 0.18f, c.y); lineTo(c.x - u * 0.04f, c.y + u * 0.14f); lineTo(c.x + u * 0.2f, c.y - u * 0.12f) }
            drawPath(chemin, ArgentScene, style = androidx.compose.ui.graphics.drawscope.Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
    }
}
