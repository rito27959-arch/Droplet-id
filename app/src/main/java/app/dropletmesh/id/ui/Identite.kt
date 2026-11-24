// ============================================================================
// MON IDENTITÉ — ce qui identifie (vérifié, utilisable par les apps), et non
// un profil. Droplet ID ne connaît ni votre nom ni votre numéro : votre
// identité, c'est un compte et la clé de ce téléphone.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.ui.draw.clip

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.fillMaxWidth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.ChiffresDroplet
import app.dropletmesh.id.designsystem.Pastille
import io.github.neoruaa.glasmio.ui.GlasmioListRow

@Composable
fun Identite(ui: UiApp, nav: Navigation) {
    val contexte = LocalContext.current
    val c = Droplet.couleurs
    Page(titre = "Informations personnelles", retour = nav.retour) {
        // L'identifiant, en grand, comme gravé sur une plaque de métal.
        item(key = "plaque") {
            androidx.compose.foundation.layout.Column(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp)
                    .clip(com.kyant.capsule.ContinuousRoundedRectangle(24.dp))
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color(0xFF26282D), androidx.compose.ui.graphics.Color(0xFF111215))))
                    .padding(22.dp),
            ) {
                Text("Identifiant Droplet", style = TextStyle(fontFamily = app.dropletmesh.id.designsystem.TexteDroplet, fontSize = 13.sp), color = app.dropletmesh.id.designsystem.ArgentScene2)
                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                Text(
                    idAffiche(ui.compte.compteId),
                    style = TextStyle(fontFamily = app.dropletmesh.id.designsystem.TitresDroplet, fontSize = 26.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, letterSpacing = 1.sp, fontFeatureSettings = "tnum"),
                    color = androidx.compose.ui.graphics.Color.White,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.height(14.dp))
                Text("Créé sur ${ui.appareil.modele}", style = TextStyle(fontFamily = app.dropletmesh.id.designsystem.TexteDroplet, fontSize = 13.sp), color = app.dropletmesh.id.designsystem.ArgentScene2)
            }
        }
        groupe("identifiants", titre = "Identifiants", pied = "Chaque app reçoit un autre identifiant, calculé pour elle seule : deux apps ne peuvent pas savoir qu'il s'agit de vous.") {
            GlasmioListRow(
                title = "Identifiant Droplet",
                leading = { Pastille(Icone.IDENTITE) },
                trailing = {
                    Text(idAffiche(ui.compte.compteId), style = TextStyle(fontFamily = ChiffresDroplet, fontSize = 15.sp), color = c.texteSecondaire)
                },
                onClick = { copier(contexte, "Identifiant Droplet", ui.compte.compteId.orEmpty()) },
                showTopSeparator = false,
            )
            GlasmioListRow(
                title = "Appareil de confiance",
                subtitle = "${ui.appareil.modele} · Android ${ui.appareil.versionAndroid}",
                leading = { Pastille(Icone.TELEPHONE) },
            )
            ui.compte.creeLe?.let {
                GlasmioListRow(
                    title = "Créée le",
                    detail = dateLongue(it),
                    leading = { Pastille(Icone.HISTORIQUE) },
                )
            }
        }
        groupe("donnees", titre = "Ce que Droplet ID sait de vous", pied = "Un pseudo n'est envoyé à une app que si vous le choisissez, au moment de vous connecter, et pour elle seule.") {
            GlasmioListRow(title = "Nom", detail = "Aucun", leading = { Pastille(Icone.PERSONNE) }, showTopSeparator = false)
            GlasmioListRow(title = "E-mail", detail = "Aucun", leading = { Pastille(Icone.EMAIL) })
            GlasmioListRow(title = "Numéro de téléphone", detail = "Aucun", leading = { Pastille(Icone.NUMERO) })
        }
    }
}

/** Copie sans aperçu dans la barre système (Android 13+ : contenu marqué sensible). */
fun copier(contexte: Context, libelle: String, texte: String) {
    val presse = contexte.getSystemService(ClipboardManager::class.java) ?: return
    val clip = ClipData.newPlainText(libelle, texte)
    if (Build.VERSION.SDK_INT >= 33) {
        clip.description.extras = PersistableBundle().apply { putBoolean("android.content.extra.IS_SENSITIVE", true) }
    }
    presse.setPrimaryClip(clip)
}
