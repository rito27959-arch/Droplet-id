// ============================================================================
// LE COMPTE — la seule page racine, construite comme une page de compte :
//   l'en-tête (avatar, nom, identifiant), puis des groupes de lignes :
//   le compte, les apps, ce téléphone, les préférences, et tout en bas,
//   seule et en rouge, l'action qui efface.
// Une seule couleur : la goutte (la lueur de l'avatar, la tuile des apps).
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.dropletmesh.id.Chargement
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.designsystem.AvatarDroplet
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.Pastille
import app.dropletmesh.id.designsystem.StyleChiffres
import app.dropletmesh.id.designsystem.TexteDroplet
import app.dropletmesh.id.designsystem.TitresDroplet
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonRole
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonSpec
import io.github.neoruaa.glasmio.ui.GlasmioAlertDialog
import io.github.neoruaa.glasmio.ui.GlasmioGroupedList
import io.github.neoruaa.glasmio.ui.GlasmioIconButton
import io.github.neoruaa.glasmio.ui.GlasmioListRow

@Composable
fun Accueil(ui: UiApp, nav: Navigation, onScanner: () -> Unit, onOnglet: (Onglet) -> Unit) {
    val c = Droplet.couleurs
    val contexte = LocalContext.current
    val actions = ui.appareil.actions
    val apps = (ui.connexions as? Chargement.Pret)?.valeur?.size
    // Une page à grand titre qui se replie au défilement (Glasmio, façon iOS).
    Page(
        titre = "Droplet ID",
        avecBarre = true,
        actions = {
            GlasmioIconButton(onClick = onScanner) { GlasmioIcon(Icone.SCANNER, contentDescription = "Scanner un code de connexion") }
        },
    ) {
        // L'en-tête : le logo bouclé, l'identifiant.
        item(key = "entete") {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarDroplet(taille = 96.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    if (actions.isEmpty()) "Votre identité est bouclée." else "Votre identité demande votre attention.",
                    Modifier.semantics { heading() },
                    style = TextStyle(fontFamily = TitresDroplet, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.015).em),
                    color = c.texte,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    idAffiche(ui.compte.compteId),
                    Modifier.clickable(role = Role.Button, onClickLabel = "Copier l'identifiant") {
                        copier(contexte, "Identifiant Droplet", ui.compte.compteId.orEmpty())
                    },
                    style = TextStyle(fontFamily = TexteDroplet, fontSize = 15.sp, lineHeight = 20.sp, fontFeatureSettings = StyleChiffres),
                    color = c.texteSecondaire,
                )
            }
        }
        groupe("compte") {
            GlasmioListRow(title = "Informations personnelles", leading = { Pastille(Icone.PERSONNE) }, onClick = { nav.ouvrir(Ecran.Identite) }, showTopSeparator = false)
            GlasmioListRow(
                title = "Sécurité",
                detail = when (actions.size) { 0 -> "Protégé"; 1 -> "1 action"; else -> "${actions.size} actions" },
                leading = { Pastille(Icone.BOUCLIER) },
                onClick = { onOnglet(Onglet.Securite) },
            )
        }
        groupe("apps") {
            GlasmioListRow(title = "Apps connectées", detail = apps?.toString(), leading = { Pastille(Icone.APPS) }, onClick = { onOnglet(Onglet.Apps) }, showTopSeparator = false)
            GlasmioListRow(title = "Se connecter avec un code", leading = { Pastille(Icone.SCANNER) }, onClick = onScanner)
        }
        groupe("appareil", pied = "Votre clé est sur ce téléphone et nulle part ailleurs.") {
            GlasmioListRow(
                title = ui.appareil.modele,
                subtitle = "Ce téléphone",
                leading = { GlasmioIcon(Icone.TELEPHONE, contentDescription = null, tint = c.texteSecondaire, size = 30.dp) },
                onClick = { onOnglet(Onglet.Securite) },
                showTopSeparator = false,
            )
        }
    }
}

/** L'identifiant en groupes de quatre, lisible à voix haute. */
fun idAffiche(id: String?): String = id?.chunked(4)?.joinToString(" ") ?: "—"
