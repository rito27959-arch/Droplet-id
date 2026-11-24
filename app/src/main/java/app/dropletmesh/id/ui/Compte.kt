// ============================================================================
// COMPTE — préférences et confidentialité : apparence, langue, ce qui est
// conservé, à propos. La langue passe par le réglage par app d'Android.
// ============================================================================
package app.dropletmesh.id.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.dropletmesh.id.BuildConfig
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.donnees.Apparence
import app.dropletmesh.id.designsystem.Corps
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.Pastille
import io.github.neoruaa.glasmio.ui.GlasmioListRow
import io.github.neoruaa.glasmio.ui.GlasmioSegmentedControl
import io.github.neoruaa.glasmio.ui.GlasmioSwitch
import java.util.Locale

@Composable
fun ReglageApparence(ui: UiApp, nav: Navigation, onApparence: (Apparence) -> Unit, onTransparence: (Boolean) -> Unit) {
    Page(titre = "Apparence", retour = nav.retour, avecBarre = false) {
        item(key = "mode") {
            GlasmioSegmentedControl(
                items = Apparence.entries.map { it to it.titre },
                selected = ui.apparence,
                onSelected = onApparence,
            )
        }
        groupe("verre", pied = "Remplace le verre des barres et des menus par des surfaces pleines. Plus lisible, et plus économe en batterie.") {
            GlasmioListRow(
                title = "Réduire la transparence",
                trailing = { GlasmioSwitch(checked = ui.reduireTransparence, onCheckedChange = onTransparence) },
                showTopSeparator = false,
            )
        }
    }
}

@Composable
fun Confidentialite(nav: Navigation) {
    Page(titre = "Confidentialité", retour = nav.retour, avecBarre = false) {
        groupe("telephone", titre = "Sur ce téléphone", pied = "Chiffré, exclu des sauvegardes. « Oublier ce téléphone » efface tout.") {
            GlasmioListRow(title = "La clé de l'appareil", subtitle = "Pour signer vos approbations", leading = { Pastille(Icone.PASSKEY) }, showTopSeparator = false)
            GlasmioListRow(title = "Vos identifiants de compte", subtitle = "Compte et appareil", leading = { Pastille(Icone.IDENTITE) })
            GlasmioListRow(title = "Vos préférences", subtitle = "Apparence, présentation déjà vue", leading = { Pastille(Icone.COMPTE) })
        }
        groupe("serveur", titre = "Sur le service Droplet ID", pied = "Le service ne reçoit jamais votre numéro, votre e-mail, votre nom, vos contacts ni vos messages.") {
            GlasmioListRow(title = "La clé publique de ce téléphone", subtitle = "Pour vérifier vos signatures", leading = { Pastille(Icone.SCEAU) }, showTopSeparator = false)
            GlasmioListRow(title = "Le nom de l'appareil", subtitle = "Le modèle, par exemple « Pixel 8 »", leading = { Pastille(Icone.TELEPHONE) })
            GlasmioListRow(title = "Les apps que vous avez autorisées", subtitle = "Et le pseudo que vous leur avez donné, s'il y en a un", leading = { Pastille(Icone.APPS) })
        }
        item(key = "mesure") {
            Corps("Aucune mesure d'audience, aucune publicité dans Droplet ID.", secondaire = true)
        }
    }
}

@Composable
fun APropos(nav: Navigation) {
    Page(titre = "À propos", retour = nav.retour, avecBarre = false) {
        groupe("version", titre = "Droplet ID") {
            GlasmioListRow(title = "Version", detail = BuildConfig.VERSION_NAME, showTopSeparator = false)
            GlasmioListRow(title = "Service", detail = "id.dropletmesh.app")
        }
        groupe("licences", titre = "Logiciels libres", pied = "Merci à leurs auteurs.") {
            GlasmioListRow(title = "Glasmio", subtitle = "Composants et verre · Apache 2.0", showTopSeparator = false)
            GlasmioListRow(title = "Inter, Inter Tight", subtitle = "Rasmus Andersson · SIL Open Font License 1.1")
            GlasmioListRow(title = "Tink", subtitle = "Chiffrement · Google · Apache 2.0")
            GlasmioListRow(title = "OkHttp", subtitle = "Réseau · Square · Apache 2.0")
            GlasmioListRow(title = "ZXing", subtitle = "Lecture des codes QR · Apache 2.0")
            GlasmioListRow(title = "AndroidX, Jetpack Compose, CameraX", subtitle = "Google · Apache 2.0")
            GlasmioListRow(title = "Icônes Material Symbols", subtitle = "Google · Apache 2.0")
        }
    }
}
