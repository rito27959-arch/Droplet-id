// ============================================================================
// L'ACCUEIL — « qui suis-je ici, et tout va-t-il bien ? ».
// Dans cet ordre : la carte, l'état de sécurité (la seule chose qui peut
// demander d'agir), au plus une action recommandée, les raccourcis.
// Pas de note sur 100 : un état en une phrase, puis quoi faire.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dropletmesh.id.Chargement
import app.dropletmesh.id.UiApp
import app.dropletmesh.id.designsystem.CarteIdentite
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.Icone
import app.dropletmesh.id.designsystem.Pastille
import app.dropletmesh.id.designsystem.SansTexte
import io.github.neoruaa.glasmio.icons.GlasmioIcon
import io.github.neoruaa.glasmio.ui.GlasmioIconButton
import io.github.neoruaa.glasmio.ui.GlasmioListRow

@Composable
fun Accueil(ui: UiApp, nav: Navigation, onRafraichir: () -> Unit) {
    val c = Droplet.couleurs
    val actions = ui.appareil.actions
    val connexions = ui.connexions
    Page(
        titre = "Accueil",
        actions = {
            GlasmioIconButton(onClick = { nav.ouvrir(Ecran.Scanner) }) {
                GlasmioIcon(Icone.SCANNER, contentDescription = "Scanner un code de connexion")
            }
        },
    ) {
        item(key = "carte") {
            CarteIdentite(
                compteId = ui.compte.compteId,
                ligneBas = ui.compte.creeLe?.let { "Créée le ${dateLongue(it)}" } ?: "Activée sur ce téléphone",
                ligneBasDroite = ui.appareil.modele,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
        }
        groupe("etat") {
            if (actions.isEmpty()) {
                GlasmioListRow(
                    title = "Votre compte est protégé",
                    subtitle = "Clé sur ce téléphone, verrouillage actif",
                    leading = { Pastille(Icone.BOUCLIER, teinte = c.succes) },
                    onClick = { nav.onglet(Onglet.Securite) },
                    showTopSeparator = false,
                )
            } else {
                GlasmioListRow(
                    title = if (actions.size == 1) "1 action recommandée" else "${actions.size} actions recommandées",
                    subtitle = actions.first().titre,
                    leading = { Pastille(Icone.ALERTE, teinte = c.erreur) },
                    onClick = { nav.onglet(Onglet.Securite) },
                    showTopSeparator = false,
                )
            }
        }
        groupe("raccourcis", titre = "Raccourcis") {
            GlasmioListRow(
                title = "Se connecter avec un code",
                subtitle = "Scannez le code d'un site « Continuer avec Droplet ID »",
                leading = { Pastille(Icone.SCANNER) },
                onClick = { nav.ouvrir(Ecran.Scanner) },
                showTopSeparator = false,
            )
            GlasmioListRow(
                title = "Applications connectées",
                detail = (connexions as? Chargement.Pret)?.valeur?.size?.toString(),
                leading = { Pastille(Icone.APPS) },
                onClick = { nav.onglet(Onglet.Acces) },
            )
            GlasmioListRow(
                title = "Ma carte d'identité",
                leading = { Pastille(Icone.CARTE) },
                onClick = { nav.onglet(Onglet.Identite) },
            )
        }
        item(key = "derniere") {
            val texte = when (connexions) {
                is Chargement.Pret -> connexions.valeur.firstOrNull()
                    ?.let { "Dernier accès accordé : ${it.nom}, ${ilYA(it.accordeLe)}" }
                    ?: "Aucune app connectée pour l'instant."
                is Chargement.Echec -> connexions.erreur.texte
                Chargement.EnCours -> ""
            }
            Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(texte, style = TextStyle(fontFamily = SansTexte, fontSize = 13.sp, lineHeight = 18.sp), color = c.texteSecondaire)
            }
        }
    }
}
