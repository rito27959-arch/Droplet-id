// ============================================================================
// MA SÉCURITÉ — ce qui protège le compte, et ce qui manque. Chaque action
// recommandée se répare en un geste (elle ouvre le bon réglage d'Android).
// ============================================================================
package app.dropletmesh.id.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.donnees.ActionSecurite
import app.dropletmesh.id.designsystem.BoutonTexte
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.Pastille
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonRole
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonSpec
import io.github.neoruaa.glasmio.ui.GlasmioAlertDialog
import io.github.neoruaa.glasmio.ui.GlasmioListRow

@Composable
fun Securite(ui: UiApp, nav: Navigation, onOublier: () -> Unit, enOnglet: Boolean = false) {
    val contexte = LocalContext.current
    val c = Droplet.couleurs
    val a = ui.appareil
    var oublier by remember { mutableStateOf(false) }
    fun ouvrirReglage(action: ActionSecurite) {
        val intent = when (action) {
            ActionSecurite.VerrouEcran -> Intent(Settings.ACTION_SECURITY_SETTINGS)
            ActionSecurite.Biometrie -> Intent(Settings.ACTION_BIOMETRIC_ENROLL)
        }
        runCatching { contexte.startActivity(intent) }
            .onFailure { runCatching { contexte.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) } }
    }
    Page(titre = if (enOnglet) "Sécurité" else "Connexion et sécurité", retour = if (enOnglet) null else nav.retour, avecBarre = enOnglet, sousTitre = if (a.actions.isEmpty()) "Votre compte est protégé" else "Des actions sont recommandées") {
        if (a.actions.isNotEmpty()) {
            groupe("actions", titre = "Actions recommandées") {
                a.actions.forEachIndexed { i, action ->
                    GlasmioListRow(
                        title = action.titre,
                        subtitle = action.explication,
                        leading = { Pastille(Icone.ALERTE, teinte = c.erreur) },
                        onClick = { ouvrirReglage(action) },
                        showTopSeparator = i > 0,
                    )
                }
            }
        }
        groupe("connexion", titre = "Connexion", pied = "Pour vous connecter, vous approuvez sur ce téléphone, puis vous tapez le nombre affiché sur l'écran de connexion. Un site qui n'affiche pas de nombre n'est pas Droplet ID.") {
            GlasmioListRow(
                title = "Clé de ce téléphone",
                subtitle = "Ed25519, créée ici, jamais envoyée",
                leading = { Pastille(Icone.PASSKEY) },
                showTopSeparator = false,
            )
            GlasmioListRow(
                title = "Mot de passe",
                detail = "Aucun",
                leading = { Pastille(Icone.MOT_DE_PASSE) },
            )
            GlasmioListRow(
                title = "Nombre de confirmation",
                detail = "Toujours",
                leading = { Pastille(Icone.DOUBLE_FACTEUR) },
            )
        }
        groupe("appareil", titre = "Ce téléphone", pied = if (a.strongBox) "La clé qui chiffre votre clé Droplet ID est gardée par la puce de sécurité du téléphone." else "La clé qui chiffre votre clé Droplet ID est gardée par le Keystore Android : elle ne peut pas être copiée hors du téléphone.") {
            GlasmioListRow(
                title = "Verrouillage de l'écran",
                detail = if (a.verrouEcran) "Activé" else "Désactivé",
                leading = { Pastille(Icone.SECURITE, teinte = if (a.verrouEcran) null else c.erreur) },
                onClick = { ouvrirReglage(ActionSecurite.VerrouEcran) },
                showTopSeparator = false,
            )
            GlasmioListRow(
                title = "Empreinte ou visage",
                detail = if (a.biometrie) "Enregistré" else "Non enregistré",
                leading = { Pastille(Icone.PASSKEY) },
                onClick = { ouvrirReglage(ActionSecurite.Biometrie) },
            )
            GlasmioListRow(
                title = "Puce de sécurité",
                detail = if (a.strongBox) "StrongBox" else "Keystore",
                leading = { Pastille(Icone.BOUCLIER) },
            )
            GlasmioListRow(
                title = "Sauvegardes",
                detail = "Exclues",
                subtitle = "Votre clé ne part ni dans le cloud ni vers un autre téléphone.",
                leading = { Pastille(Icone.EXPORT) },
            )
        }
        groupe("recuperation", titre = "Si vous perdez ce téléphone", pied = "Votre identité ne vit que sur ce téléphone. Pour l'instant, la perdre veut dire en recréer une et vous reconnecter à vos apps. Une récupération depuis un autre appareil est prévue.") {
            GlasmioListRow(
                title = "Récupération",
                detail = "Pas encore disponible",
                leading = { Pastille(Icone.RECUPERATION) },
                showTopSeparator = false,
            )
        }
        item(key = "oublier") {
            BoutonTexte("Oublier Droplet ID sur ce téléphone", onClick = { oublier = true }, destructif = true, modifier = Modifier.padding(top = 12.dp))
        }
    }
    GlasmioAlertDialog(
        visible = oublier,
        onDismissRequest = { oublier = false },
        title = "Oublier Droplet ID ?",
        message = "La clé de ce téléphone sera effacée et l'appareil retiré de votre compte. Les apps connectées ne vous reconnaîtront plus. Cette action est définitive.",
        buttons = listOf(
            GlasmioAlertButtonSpec("Annuler", { oublier = false }, role = GlasmioAlertButtonRole.Cancel),
            GlasmioAlertButtonSpec("Oublier", { oublier = false; onOublier() }, role = GlasmioAlertButtonRole.Destructive),
        ),
    )
}
