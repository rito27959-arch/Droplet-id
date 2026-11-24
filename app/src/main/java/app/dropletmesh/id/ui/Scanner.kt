// ============================================================================
// LE SCANNER — le code QR d'un site « Continuer avec Droplet ID ».
// CameraX pour l'image, ZXing pour la lecture : aucun service Google Play.
// Seuls les liens https://id[-test].dropletmesh.app/a/<demande> sont acceptés.
// ============================================================================
package app.dropletmesh.id.ui

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
import app.dropletmesh.id.designsystem.Goutte
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
            Viseur(Modifier.align(Alignment.Center).size(260.dp))
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
                    BoutonPrincipal("Autoriser la caméra", onClick = { demande.launch(Manifest.permission.CAMERA) })
                }
                else -> {
                    Text(
                        "Scannez le code",
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                    Text(
                        avis ?: "Sur le site, choisissez « Continuer avec Droplet ID » et visez le code affiché.",
                        style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, lineHeight = 21.sp),
                        color = if (avis != null) Color(0xFFFF4D5E) else Color(0xFFC9C8C4),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Les quatre coins du viseur, de la couleur goutte. */
@Composable
private fun Viseur(modifier: Modifier) {
    Canvas(modifier) {
        val l = size.minDimension * 0.16f
        val e = 4.dp.toPx()
        val w = size.width
        val h = size.height
        fun coin(a: Offset, b: Offset, c: Offset) {
            drawLine(Goutte, a, b, e, StrokeCap.Round)
            drawLine(Goutte, a, c, e, StrokeCap.Round)
        }
        coin(Offset(0f, 0f), Offset(l, 0f), Offset(0f, l))
        coin(Offset(w, 0f), Offset(w - l, 0f), Offset(w, l))
        coin(Offset(0f, h), Offset(l, h), Offset(0f, h - l))
        coin(Offset(w, h), Offset(w - l, h), Offset(w, h - l))
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
