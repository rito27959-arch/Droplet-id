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
fun Accueil(ui: UiApp, nav: Navigation, onOublier: () -> Unit) {
    val c = Droplet.couleurs
    val contexte = LocalContext.current
    val actions = ui.appareil.actions
    val apps = (ui.connexions as? Chargement.Pret)?.valeur?.size
    var oublier by remember { mutableStateOf(false) }
    val haut = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bas = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = haut + 56.dp, bottom = bas + 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // L'en-tête : qui je suis ici.
            item(key = "entete") {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AvatarDroplet(taille = 92.dp)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Droplet ID",
                        Modifier.semantics { heading() },
                        style = TextStyle(fontFamily = TitresDroplet, fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
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

            // Le compte.
            item(key = "compte") {
                GlasmioGroupedList {
                    GlasmioListRow(
                        title = "Informations personnelles",
                        onClick = { nav.ouvrir(Ecran.Identite) },
                        showTopSeparator = false,
                    )
                    GlasmioListRow(
                        title = "Connexion et sécurité",
                        detail = when (actions.size) { 0 -> null; 1 -> "1 action"; else -> "${actions.size} actions" },
                        onClick = { nav.ouvrir(Ecran.Securite) },
                    )
                    GlasmioListRow(
                        title = "Confidentialité",
                        onClick = { nav.ouvrir(Ecran.Confidentialite) },
                    )
                }
            }

            // Les apps.
            item(key = "apps") {
                GlasmioGroupedList {
                    GlasmioListRow(
                        title = "Apps connectées",
                        detail = apps?.toString(),
                        leading = { Pastille(Icone.APPS) },
                        onClick = { nav.ouvrir(Ecran.Acces) },
                        showTopSeparator = false,
                    )
                    GlasmioListRow(
                        title = "Se connecter avec un code",
                        leading = { Pastille(Icone.SCANNER) },
                        onClick = { nav.ouvrir(Ecran.Scanner) },
                    )
                }
            }

            // Ce téléphone, comme la liste des appareils d'un compte.
            item(key = "appareil") {
                GlasmioGroupedList {
                    GlasmioListRow(
                        title = ui.appareil.modele,
                        subtitle = "Ce téléphone",
                        leading = { GlasmioIcon(Icone.TELEPHONE, contentDescription = null, tint = c.texteSecondaire, size = 30.dp) },
                        onClick = { nav.ouvrir(Ecran.Securite) },
                        showTopSeparator = false,
                    )
                }
            }

            // Les préférences de l'app.
            item(key = "preferences") {
                GlasmioGroupedList {
                    GlasmioListRow(
                        title = "Apparence",
                        detail = ui.apparence.titre,
                        onClick = { nav.ouvrir(Ecran.Apparence) },
                        showTopSeparator = false,
                    )
                    GlasmioListRow(
                        title = "À propos",
                        onClick = { nav.ouvrir(Ecran.APropos) },
                    )
                }
            }

            // Seule, en bas, l'action qui efface.
            item(key = "oublier") {
                GlasmioGroupedList {
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) { oublier = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Oublier ce téléphone", style = TextStyle(fontFamily = TexteDroplet, fontSize = 17.sp, letterSpacing = (-0.024).em), color = c.erreur)
                    }
                }
            }
        }

        // Le seul bouton de la barre : scanner un code de connexion.
        Row(Modifier.fillMaxWidth().padding(top = haut + 4.dp, end = 12.dp), horizontalArrangement = Arrangement.End) {
            GlasmioIconButton(onClick = { nav.ouvrir(Ecran.Scanner) }) {
                GlasmioIcon(Icone.SCANNER, contentDescription = "Scanner un code de connexion")
            }
        }
    }

    GlasmioAlertDialog(
        visible = oublier,
        onDismissRequest = { oublier = false },
        title = "Oublier ce téléphone ?",
        message = "La clé de ce téléphone sera effacée et l'appareil retiré de votre compte. Les apps connectées ne vous reconnaîtront plus. Cette action est définitive.",
        buttons = listOf(
            GlasmioAlertButtonSpec("Annuler", { oublier = false }, role = GlasmioAlertButtonRole.Cancel),
            GlasmioAlertButtonSpec("Oublier", { oublier = false; onOublier() }, role = GlasmioAlertButtonRole.Destructive),
        ),
    )
}

/** L'identifiant en groupes de quatre, lisible à voix haute. */
fun idAffiche(id: String?): String = id?.chunked(4)?.joinToString(" ") ?: "—"
