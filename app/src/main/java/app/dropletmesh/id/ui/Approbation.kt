// ============================================================================
// APPROUVER UNE CONNEXION — qui demande, quoi, et le nombre affiché sur
// l'écran de connexion. Approuver demande le déverrouillage du téléphone
// (empreinte, visage ou code). Le nombre est ce qui empêche d'approuver par
// réflexe une demande lancée par quelqu'un d'autre.
// ============================================================================
package app.dropletmesh.id.ui

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
import app.dropletmesh.id.designsystem.MonoIdentifiants
import app.dropletmesh.id.designsystem.SansTexte
import app.dropletmesh.id.designsystem.Sceau
import app.dropletmesh.id.designsystem.SerifTitres
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
    val c = Droplet.couleurs
    Box(
        Modifier.fillMaxSize().background(c.fond).background(
            Brush.verticalGradient(0f to c.laiton.copy(alpha = if (c.sombre) 0.10f else 0.06f), 0.32f to c.laiton.copy(alpha = 0f)),
        ),
    ) {
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
                style = TextStyle(fontFamily = SansTexte, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                color = c.laiton,
                modifier = Modifier.padding(start = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            GlasmioIconButton(onClick = onFermer) { GlasmioIcon("xmark", contentDescription = "Fermer sans décider") }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Initiale(d.nomApp, taille = 56)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(d.nomApp, style = TextStyle(fontFamily = SansTexte, fontSize = 17.sp, fontWeight = FontWeight.SemiBold), color = c.texte)
                    if (d.site.isNotEmpty()) {
                        Text(d.site, style = TextStyle(fontFamily = MonoIdentifiants, fontSize = 13.sp), color = c.texteSecondaire)
                    }
                }
            }
            Text(
                "Se connecter à ${d.nomApp} ?",
                Modifier.semantics { heading() },
                style = TextStyle(fontFamily = SerifTitres, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
                color = c.texte,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Corps("Tapez le nombre affiché sur l'écran de connexion.", secondaire = true)
                ChampCode(code, { code = it.filter(Char::isDigit).take(2) }, Modifier.focusRequester(focus))
            }
            Column {
                Text(
                    "${d.nomApp} recevra",
                    Modifier.padding(start = 4.dp, bottom = 6.dp),
                    style = TextStyle(fontFamily = SansTexte, fontSize = 14.sp, fontWeight = FontWeight.Medium),
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
                    GlasmioTextField(
                        value = pseudo,
                        onValueChange = { pseudo = it.take(40) },
                        placeholder = "Pseudo pour ${d.nomApp}",
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    )
                }
            }
            if (!appareil.verrouEcran) {
                Avertissement("Ce téléphone n'a pas de verrouillage d'écran : rien ne vérifie que c'est vous qui approuvez.")
            }
            etat.erreur?.let { Avertissement(it) }
            Corps("Pas d'écran de connexion devant vous ? Refusez : quelqu'un essaie peut-être d'entrer dans votre compte.", secondaire = true)
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BoutonPrincipal(
                if (etat.occupe) "Approbation…" else "Approuver",
                actif = code.length == 2 && !etat.occupe,
                onClick = {
                    deverrouiller(contexte, "Se connecter à ${d.nomApp}", appareil.verrouEcran) {
                        onApprouver(code, partager, pseudo)
                    }
                },
            )
            BoutonTexte("Refuser", onClick = onRefuser, destructif = true, actif = !etat.occupe)
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
        modifier = modifier.semantics { contentDescription = "Nombre à deux chiffres" },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(fontFamily = MonoIdentifiants, fontSize = 1.sp, color = c.fond),
        cursorBrush = SolidColor(c.fond),
        decorationBox = { interne ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) { i ->
                    val chiffre = valeur.getOrNull(i)?.toString().orEmpty()
                    val actif = i == valeur.length.coerceAtMost(1)
                    Box(
                        Modifier.size(width = 72.dp, height = 84.dp).clip(RoundedCornerShape(18.dp)).background(c.surface)
                            .border(if (actif) 2.dp else 1.dp, if (actif) c.laiton else c.bordure, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(chiffre, style = TextStyle(fontFamily = MonoIdentifiants, fontSize = 40.sp, fontWeight = FontWeight.Medium), color = c.texte)
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
        Text(texte, style = TextStyle(fontFamily = SansTexte, fontSize = 15.sp, lineHeight = 21.sp), color = c.texte)
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
            sceau = true,
            titre = "Connexion approuvée",
            texte = if (etat.depuisLien) "Retour sur $nom…" else "Revenez sur $nom : la connexion se termine.",
            bouton = if (etat.depuisLien) "Revenir au navigateur" else "Terminé",
            onFermer = onFermer,
        )
    } else {
        Fin(icone = Icone.BLOQUER, titre = "Connexion refusée", texte = "$nom ne vous a pas connecté.", bouton = "Fermer", onFermer = onFermer)
    }
}

@Composable
private fun Fin(titre: String, texte: String, bouton: String, onFermer: () -> Unit, icone: String? = null, sceau: Boolean = false) {
    val c = Droplet.couleurs
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        if (sceau) Sceau() else if (icone != null) GlasmioIcon(icone, contentDescription = null, tint = c.texteSecondaire, size = 64.dp)
        Spacer(Modifier.height(24.dp))
        Text(titre, style = TextStyle(fontFamily = SerifTitres, fontSize = 28.sp, fontWeight = FontWeight.SemiBold), color = c.texte, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Corps(texte, secondaire = true)
        Spacer(Modifier.weight(1.4f))
        BoutonPrincipal(bouton, onClick = onFermer)
    }
}

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
