// ============================================================================
// APPROUVER UNE CONNEXION — qui demande, quoi, et le nombre affiché sur
// l'écran de connexion. Approuver demande le déverrouillage du téléphone
// (empreinte, visage ou code). Le nombre est ce qui empêche d'approuver par
// réflexe une demande lancée par quelqu'un d'autre.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.foundation.clickable

import app.dropletmesh.id.designsystem.RougeBoucle

import androidx.compose.ui.unit.em

import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.graphics.Color

import androidx.compose.animation.core.spring

import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.core.Animatable

import app.dropletmesh.id.designsystem.LogoCeinture

import app.dropletmesh.id.designsystem.ArgentScene2

import app.dropletmesh.id.designsystem.ArgentScene

import app.dropletmesh.id.designsystem.ThemeDroplet

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import app.dropletmesh.id.EtatDemande
import app.dropletmesh.id.donnees.EtatAppareil
import app.dropletmesh.id.donnees.Liens
import app.dropletmesh.id.designsystem.BoutonPrincipal
import app.dropletmesh.id.designsystem.BoutonTexte
import app.dropletmesh.id.designsystem.Corps
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.ChiffresDroplet
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.Sceau
import app.dropletmesh.id.designsystem.TitresDroplet
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.ui.GlasmioGroupedList
import io.github.neoruaa.glasmio.ui.GlasmioIconButton
import io.github.neoruaa.glasmio.ui.GlasmioListRow
import io.github.neoruaa.glasmio.ui.GlasmioSwitch
import io.github.neoruaa.glasmio.ui.GlasmioTextField
import kotlinx.coroutines.delay

@Composable
fun Approbation(
    etat: EtatDemande,
    appareil: EtatAppareil,
    onApprouver: (code: String, partagerPseudo: Boolean, pseudo: String) -> Unit,
    onRefuser: () -> Unit,
    onFermer: () -> Unit,
) {
    // Un moment de confiance : toujours sur la scène noire, comme l'ouverture.
    ThemeDroplet(sombre = true) {
    FondScene(lumiere = 0.5f) {
        AnimatedContent(
            targetState = when {
                etat.resultat != null -> 2
                etat.demande == null && etat.erreur != null -> 1
                etat.demande == null -> 0
                else -> 3
            },
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
            label = "approbation",
        ) { vue ->
            when (vue) {
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Corps("Lecture de la demande…", secondaire = true) }
                1 -> Fin(icone = Icone.ALERTE, titre = "Demande impossible", texte = etat.erreur.orEmpty(), bouton = "Fermer", onFermer = onFermer)
                2 -> Resultat(etat, onFermer)
                else -> Formulaire(etat, appareil, onApprouver, onRefuser, onFermer)
            }
        }
    }
    }
}

@Composable
private fun Formulaire(
    etat: EtatDemande,
    appareil: EtatAppareil,
    onApprouver: (String, Boolean, String) -> Unit,
    onRefuser: () -> Unit,
    onFermer: () -> Unit,
) {
    val c = Droplet.couleurs
    val d = etat.demande ?: return
    val contexte = LocalContext.current
    var code by rememberSaveable { mutableStateOf("") }
    var partager by rememberSaveable { mutableStateOf(true) }
    var pseudo by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (etat.lien.emetteur == Liens.TEST) "DROPLET ID · TEST" else "DROPLET ID",
                style = TextStyle(fontFamily = TexteDroplet, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                color = ArgentScene2,
                modifier = Modifier.padding(start = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            GlasmioIconButton(onClick = onFermer) { GlasmioIcon("xmark", contentDescription = "Fermer sans décider") }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            // L'app et Droplet ID, reliés par un fil qui se trace.
            Lien(d.nomApp)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Se connecter à ${d.nomApp} ?",
                    Modifier.semantics { heading() },
                    style = TextStyle(fontFamily = TitresDroplet, fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.025).em, textAlign = TextAlign.Center),
                    color = Color.White,
                )
                if (d.site.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(d.site, style = TextStyle(fontFamily = TexteDroplet, fontSize = 14.sp), color = ArgentScene2)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tapez le nombre affiché sur l'écran de connexion.", Modifier.fillMaxWidth(), style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, textAlign = TextAlign.Center), color = ArgentScene2)
                ChampCode(code, { code = it.filter(Char::isDigit).take(2) }, Modifier.focusRequester(focus))
            }
            Column {
                Text(
                    "${d.nomApp} recevra",
                    Modifier.padding(start = 4.dp, bottom = 6.dp),
                    style = TextStyle(fontFamily = TexteDroplet, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    color = c.texteSecondaire,
                )
                GlasmioGroupedList {
                    GlasmioListRow(
                        title = "Un identifiant propre à ${d.nomApp}",
                        subtitle = "Nécessaire · impossible à relier à une autre app",
                        showTopSeparator = false,
                    )
                    if (d.demandeProfil) {
                        GlasmioListRow(
                            title = "Un pseudo",
                            subtitle = "Facultatif, pour ${d.nomApp} seulement",
                            trailing = { GlasmioSwitch(checked = partager, onCheckedChange = { partager = it }) },
                        )
                    }
                }
                if (d.demandeProfil && partager) {
                    // Le champ dans son propre îlot : visible comme un champ, pas comme du texte.
                    GlasmioGroupedList(Modifier.padding(top = 10.dp)) {
                        GlasmioTextField(
                            value = pseudo,
                            onValueChange = { pseudo = it.take(40) },
                            placeholder = "Pseudo pour ${d.nomApp}",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            if (!appareil.verrouEcran) {
                Avertissement("Ce téléphone n'a pas de verrouillage d'écran : rien ne vérifie que c'est vous qui approuvez.")
            }
            etat.erreur?.let { Avertissement(it) }
            Text("Pas d'écran de connexion devant vous ? Refusez : quelqu'un essaie peut-être d'entrer dans votre compte.", style = TextStyle(fontFamily = TexteDroplet, fontSize = 14.sp, lineHeight = 20.sp), color = ArgentScene2)
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BoutonMetal(
                if (etat.occupe) "Approbation…" else "Approuver",
                actif = code.length == 2 && !etat.occupe,
                brille = code.length == 2 && !etat.occupe,
                onClick = {
                    deverrouiller(contexte, "Se connecter à ${d.nomApp}", appareil.verrouEcran) {
                        onApprouver(code, partager, pseudo)
                    }
                },
            )
            Text(
                "Refuser",
                Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)).clickable(enabled = !etat.occupe, role = androidx.compose.ui.semantics.Role.Button) { onRefuser() }.padding(vertical = 16.dp),
                style = TextStyle(fontFamily = TexteDroplet, fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
                color = RougeBoucle,
            )
        }
    }
}

/** Deux chiffres, en grand, en Source Code Pro. */
@Composable
private fun ChampCode(valeur: String, onChange: (String) -> Unit, modifier: Modifier) {
    val c = Droplet.couleurs
    BasicTextField(
        value = valeur,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth().semantics { contentDescription = "Nombre à deux chiffres" },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(fontFamily = ChiffresDroplet, fontSize = 1.sp, color = c.fond),
        cursorBrush = SolidColor(c.fond),
        decorationBox = { interne ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)) {
                repeat(2) { i ->
                    val chiffre = valeur.getOrNull(i)?.toString().orEmpty()
                    val actif = i == valeur.length.coerceAtMost(1)
                    val arrivee by animateFloatAsState(if (chiffre.isEmpty()) 0f else 1f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "chiffre")
                    Box(
                        Modifier.size(width = 84.dp, height = 100.dp).clip(RoundedCornerShape(20.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF1F2024), Color(0xFF141518))))
                            .border(if (actif) 1.5.dp else 1.dp, if (actif) ArgentScene else Color(0xFF2E3035), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            chiffre,
                            Modifier.graphicsLayer { scaleX = 0.6f + 0.4f * arrivee; scaleY = scaleX; alpha = arrivee; translationY = (1 - arrivee) * 18f },
                            style = TextStyle(fontFamily = TitresDroplet, fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                            color = Color.White,
                        )
                    }
                }
                Box(Modifier.size(1.dp)) { interne() }
            }
        },
    )
}

@Composable
private fun Avertissement(texte: String) {
    val c = Droplet.couleurs
    Row(verticalAlignment = Alignment.Top) {
        GlasmioIcon(Icone.ALERTE, contentDescription = "Attention", tint = c.erreur, size = 20.dp)
        Spacer(Modifier.width(8.dp))
        Text(texte, style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, lineHeight = 21.sp), color = c.texte)
    }
}

@Composable
private fun Resultat(etat: EtatDemande, onFermer: () -> Unit) {
    val nom = etat.demande?.nomApp ?: "le site"
    if (etat.resultat == EtatDemande.Resultat.Approuvee) {
        // Le sceau se trace ; ouvert par un lien, on rend la main au navigateur.
        LaunchedEffect(Unit) {
            if (etat.depuisLien) {
                delay(1300)
                onFermer()
            }
        }
        Fin(
            boucle = true,
            titre = "Connexion approuvée",
            texte = if (etat.depuisLien) "Retour sur $nom…" else "Revenez sur $nom : la connexion se termine.",
            bouton = if (etat.depuisLien) "Revenir au navigateur" else "Terminé",
            onFermer = onFermer,
        )
    } else {
        Fin(boucle = false, titre = "Connexion refusée", texte = "$nom ne vous a pas connecté.", bouton = "Fermer", onFermer = onFermer)
    }
}

@Composable
private fun Fin(titre: String, texte: String, bouton: String, onFermer: () -> Unit, icone: String? = null, boucle: Boolean? = null) {
    val retour = LocalHapticFeedback.current
    val ceinture = remember { Animatable(if (boucle == false) 1f else 0f) }
    val eclat = remember { Animatable(0f) }
    LaunchedEffect(boucle) {
        if (boucle == null) return@LaunchedEffect
        delay(250)
        ceinture.animateTo(if (boucle) 1f else 0f, tween(560))
        if (boucle) { retour.performHapticFeedback(HapticFeedbackType.Confirm); eclat.animateTo(1f, tween(650)) }
        else retour.performHapticFeedback(HapticFeedbackType.Reject)
    }
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        if (boucle != null) {
            Box(Modifier.size(150.dp).graphicsLayer { alpha = if (boucle) 1f else 0.75f }.clip(RoundedCornerShape(30))) {
                LogoCeinture(Modifier.fillMaxSize(), boucle = ceinture.value, eclat = eclat.value)
            }
        } else if (icone != null) GlasmioIcon(icone, contentDescription = null, tint = ArgentScene2, size = 64.dp)
        Spacer(Modifier.height(28.dp))
        Text(titre, style = TextStyle(fontFamily = TitresDroplet, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.025).em), color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(texte, style = TextStyle(fontFamily = TexteDroplet, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center), color = ArgentScene2)
        Spacer(Modifier.weight(1.4f))
        BoutonMetal(bouton, onClick = onFermer)
    }
}

/** L'icône de l'app à gauche, Droplet ID à droite, et le fil qui les relie. */
@Composable
private fun Lien(nomApp: String) {
    val trace = remember { Animatable(0f) }
    LaunchedEffect(Unit) { trace.animateTo(1f, tween(900, delayMillis = 150)) }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(68.dp).graphicsLayer { val q = borneApprob(trace.value * 3f); scaleX = 0.7f + 0.3f * q; scaleY = scaleX; alpha = q }) { Initiale(nomApp, taille = 68) }
        androidx.compose.foundation.Canvas(Modifier.width(76.dp).height(20.dp)) {
            val y = size.height / 2
            val n = 7
            repeat(n) { i ->
                val q = borneApprob(trace.value * 1.6f - i * 0.1f)
                drawCircle(ArgentScene.copy(alpha = q * (if (i == n / 2) 1f else 0.6f)), if (i == n / 2) 3.5.dp.toPx() else 2.dp.toPx(), Offset(size.width * (i + 0.5f) / n, y))
            }
        }
        Box(Modifier.size(68.dp).graphicsLayer { val q = borneApprob(trace.value * 3f - 1f); scaleX = 0.7f + 0.3f * q; scaleY = scaleX; alpha = q }.clip(RoundedCornerShape(30))) {
            LogoCeinture(Modifier.fillMaxSize(), description = null)
        }
    }
}

private fun borneApprob(v: Float) = v.coerceIn(0f, 1f)

/**
 * Le déverrouillage avant d'approuver : empreinte, visage ou code de
 * l'appareil. Sans verrouillage configuré, on ne peut rien demander : la
 * demande passe (comme dans l'app Droplet), et l'écran l'a dit au-dessus.
 */
fun deverrouiller(contexte: Context, raison: String, verrouEcran: Boolean, ok: () -> Unit) {
    val activite = contexte.activite() ?: return
    if (!verrouEcran) return ok()
    val invite = BiometricPrompt(
        activite,
        ContextCompat.getMainExecutor(activite),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = ok()
        },
    )
    invite.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmer que c'est vous")
            .setSubtitle(raison)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build(),
    )
}

private tailrec fun Context.activite(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.activite()
    else -> null
}
