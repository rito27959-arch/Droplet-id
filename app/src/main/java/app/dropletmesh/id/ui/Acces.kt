// ============================================================================
// MES ACCÈS — les apps et sites qui utilisent votre identité. Les apps
// Droplet sont listées comme les autres, avec le même droit de retrait.
// Retirer un accès est plus facile que l'accorder : pas de biométrie, une
// confirmation qui dit ce qui va se passer.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.dropletmesh.id.Chargement
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.donnees.Connexion
import app.dropletmesh.id.designsystem.BoutonTexte
import app.dropletmesh.id.designsystem.Corps
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.Pastille
import app.dropletmesh.id.designsystem.TitresDroplet
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.scaffold.GlasmioPullToRefreshBox
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonRole
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonSpec
import io.github.neoruaa.glasmio.ui.GlasmioAlertDialog
import io.github.neoruaa.glasmio.ui.GlasmioIconButton
import io.github.neoruaa.glasmio.ui.GlasmioListRow

@Composable
fun Acces(ui: UiApp, nav: Navigation, onRafraichir: () -> Unit, enOnglet: Boolean = false) {
    val c = Droplet.couleurs
    val etat = ui.connexions
    GlasmioPullToRefreshBox(isRefreshing = false, onRefresh = onRafraichir) {
        Page(
            titre = "Apps connectées",
            retour = if (enOnglet) null else nav.retour,
            avecBarre = enOnglet,
            sousTitre = (etat as? Chargement.Pret)?.let { "Mis à jour ${ilYA(it.aJour)}" },
            actions = {
                GlasmioIconButton(onClick = { nav.ouvrir(Ecran.Scanner) }) {
                    GlasmioIcon(Icone.SCANNER, contentDescription = "Connecter une app avec un code")
                }
            },
        ) {
            when (etat) {
                Chargement.EnCours -> groupe("chargement") {
                    repeat(3) { i -> GlasmioListRow(title = " ", subtitle = " ", leading = { Pastille(Icone.APPS, teinte = c.bordure) }, showTopSeparator = i > 0, enabled = false) }
                }
                is Chargement.Echec -> groupe("erreur", pied = "Tirez vers le bas pour réessayer.") {
                    GlasmioListRow(
                        title = "Liste indisponible",
                        subtitle = etat.erreur.texte,
                        leading = { Pastille(Icone.ALERTE, teinte = c.erreur) },
                        onClick = onRafraichir,
                        showTopSeparator = false,
                    )
                }
                is Chargement.Pret -> if (etat.valeur.isEmpty()) {
                    item(key = "vide") {
                        Column(Modifier.fillMaxWidth().padding(top = 24.dp, start = 4.dp, end = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Aucune app connectée", style = TextStyle(fontFamily = TitresDroplet, fontSize = 20.sp, fontWeight = FontWeight.SemiBold), color = c.texte)
                            Corps("Quand vous vous connectez à une app avec Droplet ID, elle apparaît ici. Vous pourrez lui retirer l'accès à tout moment.", secondaire = true)
                        }
                    }
                } else {
                    groupe(
                        "apps",
                        titre = if (etat.valeur.size == 1) "1 app connectée" else "${etat.valeur.size} apps connectées",
                        pied = "Retirer une app la déconnecte partout où elle utilise votre identité.",
                    ) {
                        etat.valeur.forEachIndexed { i, cx ->
                            GlasmioListRow(
                                title = cx.nom,
                                subtitle = "Depuis ${ilYA(cx.accordeLe).removePrefix("le ")}",
                                leading = { Initiale(cx.nom) },
                                onClick = { nav.ouvrir(Ecran.Connexion(cx.clientId)) },
                                showTopSeparator = i > 0,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Initiale(nom: String, taille: Int = 32) {
    val c = Droplet.couleurs
    // Les apps de la famille Droplet ont leur icône officielle.
    val icone = iconeOfficielle(nom)
    if (icone != null) {
        androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(icone), null, Modifier.size(taille.dp).clip(RoundedCornerShape((taille * 0.26f).dp)))
        return
    }
    Box(
        Modifier.size(taille.dp).clip(RoundedCornerShape((taille * 0.24f).dp)).background(c.surfaceElevee),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            nom.firstOrNull()?.uppercase() ?: "?",
            style = TextStyle(fontFamily = TitresDroplet, fontSize = (taille * 0.46f).sp, fontWeight = FontWeight.Bold),
            color = c.texteSecondaire,
        )
    }
}

@Composable
fun DetailConnexion(ui: UiApp, clientId: String, nav: Navigation, onRetirer: (Connexion) -> Unit) {
    val cx = (ui.connexions as? Chargement.Pret)?.valeur?.firstOrNull { it.clientId == clientId }
    var confirmer by remember { mutableStateOf(false) }
    Page(titre = cx?.nom ?: "Application", retour = nav.retour, avecBarre = false) {
        if (cx == null) {
            item(key = "absente") { Corps("Cette app n'a plus accès à votre identité.", secondaire = true) }
            return@Page
        }
        item(key = "entete") {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // L'icône officielle, posée sur une lumière d'argent.
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(Modifier.size(160.dp)) {
                        drawCircle(androidx.compose.ui.graphics.Brush.radialGradient(listOf(androidx.compose.ui.graphics.Color(0x33C8CCD4), androidx.compose.ui.graphics.Color.Transparent)))
                    }
                    Initiale(cx.nom, taille = 88)
                }
                Spacer(Modifier.height(4.dp))
                Text(cx.nom, style = TextStyle(fontFamily = TitresDroplet, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em), color = Droplet.couleurs.texte)
                Spacer(Modifier.height(4.dp))
                Corps("Connectée ${ilYA(cx.accordeLe)}", secondaire = true)
            }
        }
        groupe("recoit", titre = "Ce que ${cx.nom} reçoit", pied = "Cet identifiant ne vaut que pour ${cx.nom}. Aucune autre app ne peut le relier à vous.") {
            GlasmioListRow(
                title = "Un identifiant propre à ${cx.nom}",
                subtitle = "Nécessaire pour vous reconnaître",
                leading = { Pastille(Icone.IDENTITE) },
                showTopSeparator = false,
            )
            GlasmioListRow(
                title = "Votre pseudo",
                detail = if (cx.partageProfil) "Partagé" else "Non partagé",
                leading = { Pastille(Icone.PERSONNE) },
            )
        }
        groupe("dates", titre = "Historique") {
            GlasmioListRow(title = "Accès accordé", detail = dateHeure(cx.accordeLe), leading = { Pastille(Icone.HISTORIQUE) }, showTopSeparator = false)
        }
        item(key = "retirer") {
            BoutonTexte("Retirer l'accès", onClick = { confirmer = true }, destructif = true, modifier = Modifier.padding(top = 16.dp))
        }
    }
    if (cx != null) {
        GlasmioAlertDialog(
            visible = confirmer,
            onDismissRequest = { confirmer = false },
            title = "Retirer l'accès de ${cx.nom} ?",
            message = "${cx.nom} vous déconnectera et ne pourra plus utiliser votre identité. Vous pourrez vous reconnecter plus tard.",
            buttons = listOf(
                GlasmioAlertButtonSpec("Annuler", { confirmer = false }, role = GlasmioAlertButtonRole.Cancel),
                GlasmioAlertButtonSpec("Retirer", { confirmer = false; onRetirer(cx) }, role = GlasmioAlertButtonRole.Destructive),
            ),
        )
    }
}

/** L'icône officielle d'une app de la famille Droplet, d'après son nom. */
fun iconeOfficielle(nom: String): Int? {
    val n = nom.lowercase()
    return when {
        "kunfox" in n -> app.dropletmesh.id.R.drawable.app_kunfox
        "drop" in n && "droplet drop" in n -> app.dropletmesh.id.R.drawable.app_drop
        "messenger" in n || n == "droplet" -> app.dropletmesh.id.R.drawable.app_messenger
        else -> null
    }
}
