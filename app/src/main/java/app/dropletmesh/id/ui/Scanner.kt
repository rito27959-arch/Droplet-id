// ============================================================================
// LE SCANNER — le code QR d'un site « Continuer avec Droplet ID ».
// CameraX pour l'image, ZXing pour la lecture : aucun service Google Play.
// Seuls les liens https://id[-test].dropletmesh.app/a/<demande> sont acceptés.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.ui.unit.em

import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.graphics.drawscope.clipPath

import androidx.compose.ui.geometry.CornerRadius

import androidx.compose.ui.geometry.RoundRect

import androidx.compose.ui.graphics.Path

import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.core.spring

import androidx.compose.animation.core.tween

import androidx.compose.animation.core.LinearEasing

import androidx.compose.animation.core.RepeatMode

import androidx.compose.animation.core.infiniteRepeatable

import androidx.compose.animation.core.animateFloat

import androidx.compose.animation.core.rememberInfiniteTransition

import app.dropletmesh.id.designsystem.ArgentScene2

import app.dropletmesh.id.designsystem.RougeBoucle

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.dropletmesh.id.donnees.LienDemande
import app.dropletmesh.id.donnees.Liens
import app.dropletmesh.id.designsystem.BoutonPrincipal
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.TitresDroplet
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.ui.GlasmioIconButton
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun Scanner(onLien: (LienDemande) -> Unit, onFermer: () -> Unit) {
    val contexte = LocalContext.current
    var autorise by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(contexte, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var refuse by remember { mutableStateOf(false) }
    val demande = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        autorise = ok
        refuse = !ok
    }
    LaunchedEffect(Unit) { if (!autorise) demande.launch(Manifest.permission.CAMERA) }
    var avis by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(avis) {
        if (avis != null) {
            delay(2500)
            avis = null
        }
    }
    var trouve by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (autorise) {
            ApercuCamera { texte ->
                if (trouve) return@ApercuCamera
                val lien = Liens.demande(texte)
                if (lien != null) {
                    trouve = true
                    onLien(lien)
                } else {
                    avis = "Ce code n'est pas une connexion Droplet ID."
                }
            }
            Voile(Modifier.fillMaxSize(), trouve)
        }
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlasmioIconButton(onClick = onFermer) { GlasmioIcon("xmark", contentDescription = "Fermer", tint = Color.White) }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                refuse -> {
                    Text(
                        "Droplet ID a besoin de la caméra pour lire le code. L'image n'est ni enregistrée ni envoyée.",
                        style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 23.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    BoutonMetal("Autoriser la caméra", onClick = { demande.launch(Manifest.permission.CAMERA) })
                }
                else -> {
                    Text(
                        if (trouve) "Code reconnu" else "Scannez le code",
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
                        color = Color.White,
                    )
                    Text(
                        avis ?: "Sur le site, choisissez « Continuer avec Droplet ID » et visez le code affiché.",
                        style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, lineHeight = 21.sp),
                        color = if (avis != null) RougeBoucle else ArgentScene2,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * Le voile sur la caméra : tout s'assombrit sauf la fenêtre de visée, aux
 * coins d'argent qui respirent. Une ligne rouge balaie la fenêtre ; quand un
 * code est reconnu, les coins se resserrent et la fenêtre s'illumine.
 */
@Composable
private fun Voile(modifier: Modifier, trouve: Boolean) {
    val retour = LocalHapticFeedback.current
    LaunchedEffect(trouve) { if (trouve) retour.performHapticFeedback(HapticFeedbackType.Confirm) }
    val tr = rememberInfiniteTransition(label = "viseur")
    val souffle by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "souffle")
    val balayage by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "balayage")
    val serre by animateFloatAsState(if (trouve) 1f else 0f, spring(dampingRatio = 0.55f, stiffness = 380f), label = "serre")
    Canvas(modifier) {
        val cote = size.minDimension * 0.68f * (1f - serre * 0.12f) + souffle * 6.dp.toPx() * (1 - serre)
        val gauche = (size.width - cote) / 2; val haut = (size.height - cote) / 2 - size.height * 0.04f
        val rayon = 28.dp.toPx()
        val fenetre = Path().apply { addRoundRect(RoundRect(gauche, haut, gauche + cote, haut + cote, CornerRadius(rayon))) }
        clipPath(fenetre, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) { drawRect(Color.Black.copy(alpha = 0.62f)) }
        if (serre > 0f) drawPath(fenetre, Color.White.copy(alpha = 0.18f * serre))
        val l = cote * 0.18f; val e = 4.dp.toPx(); val d = e / 2
        val coins = Path().apply {
            moveTo(gauche + d, haut + l); lineTo(gauche + d, haut + rayon); quadraticTo(gauche + d, haut + d, gauche + rayon, haut + d); lineTo(gauche + l, haut + d)
            moveTo(gauche + cote - l, haut + d); lineTo(gauche + cote - rayon, haut + d); quadraticTo(gauche + cote - d, haut + d, gauche + cote - d, haut + rayon); lineTo(gauche + cote - d, haut + l)
            moveTo(gauche + d, haut + cote - l); lineTo(gauche + d, haut + cote - rayon); quadraticTo(gauche + d, haut + cote - d, gauche + rayon, haut + cote - d); lineTo(gauche + l, haut + cote - d)
            moveTo(gauche + cote - l, haut + cote - d); lineTo(gauche + cote - rayon, haut + cote - d); quadraticTo(gauche + cote - d, haut + cote - d, gauche + cote - d, haut + cote - rayon); lineTo(gauche + cote - d, haut + cote - l)
        }
        drawPath(coins, Color.White, style = Stroke(e, cap = StrokeCap.Round))
        if (!trouve) {
            val y = haut + cote * (0.08f + 0.84f * balayage)
            drawLine(
                Brush.horizontalGradient(listOf(Color.Transparent, RougeBoucle, Color.Transparent), gauche, gauche + cote),
                Offset(gauche + 16.dp.toPx(), y), Offset(gauche + cote - 16.dp.toPx(), y), 2.dp.toPx(),
            )
        }
    }
}

@Composable
private fun ApercuCamera(onTexte: (String) -> Unit) {
    val cycle = LocalLifecycleOwner.current
    val executeur = remember { Executors.newSingleThreadExecutor() }
    val lecteur = remember {
        MultiFormatReader().apply { setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE))) }
    }
    DisposableEffect(Unit) { onDispose { executeur.shutdown() } }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val vue = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            val futur = ProcessCameraProvider.getInstance(ctx)
            futur.addListener({
                val fournisseur = futur.get()
                val apercu = Preview.Builder().build().also { it.setSurfaceProvider(vue.surfaceProvider) }
                val analyse = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analyse.setAnalyzer(executeur) { image -> lire(image, lecteur)?.let { t -> vue.post { onTexte(t) } } }
                fournisseur.unbindAll()
                runCatching { fournisseur.bindToLifecycle(cycle, CameraSelector.DEFAULT_BACK_CAMERA, apercu, analyse) }
            }, ContextCompat.getMainExecutor(ctx))
            vue
        },
    )
}

/** Le plan de luminance (Y) suffit à ZXing. */
private fun lire(image: ImageProxy, lecteur: MultiFormatReader): String? = image.use {
    val plan = it.planes[0]
    val tampon = plan.buffer
    val octets = ByteArray(tampon.remaining()).also(tampon::get)
    val source = PlanarYUVLuminanceSource(octets, plan.rowStride, it.height, 0, 0, it.width, it.height, false)
    try {
        lecteur.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
    } catch (_: NotFoundException) {
        null
    } catch (_: Exception) {
        null
    } finally {
        lecteur.reset()
    }
}
