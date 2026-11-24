// ============================================================================
// L'ACCUEIL — « qui suis-je ici, et tout va-t-il bien ? ».
// Dans cet ordre : la carte, l'état de sécurité (la seule chose qui peut
// demander d'agir), au plus une action recommandée, les raccourcis.
// Pas de note sur 100 : un état en une phrase, puis quoi faire.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.kyant.capsule.ContinuousRoundedRectangle
import io.github.neoruaa.glasmio.glass.GlassSurface
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
        // Les raccourcis : quatre tuiles de verre (elles réfractent le fond aurore).
        item(key = "tuiles") {
            val apps = (connexions as? Chargement.Pret)?.valeur?.size
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tuile(Icone.SCANNER, "Se connecter", "Scanner un code", Modifier.weight(1f)) { nav.ouvrir(Ecran.Scanner) }
                    Tuile(Icone.APPS, "Mes apps", apps?.let { if (it == 1) "1 connectée" else "$it connectées" } ?: "—", Modifier.weight(1f)) { nav.onglet(Onglet.Acces) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tuile(Icone.CARTE, "Ma carte", "Identité Droplet", Modifier.weight(1f)) { nav.onglet(Onglet.Identite) }
                    Tuile(
                        Icone.BOUCLIER,
                        "Sécurité",
                        if (actions.isEmpty()) "Protégé" else "${actions.size} à faire",
                        Modifier.weight(1f),
                        teinte = if (actions.isEmpty()) null else c.erreur,
                    ) { nav.onglet(Onglet.Securite) }
                }
            }
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

/** Une tuile de raccourci en verre : grande icône de couleur, titre, état. */
@Composable
private fun Tuile(icone: String, titre: String, sous: String, modifier: Modifier, teinte: androidx.compose.ui.graphics.Color? = null, onClick: () -> Unit) {
    val c = Droplet.couleurs
    GlassSurface(
        modifier = modifier.height(116.dp),
        shape = ContinuousRoundedRectangle(24.dp),
        onClick = onClick,
        contentAlignment = Alignment.TopStart,
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Pastille(icone, teinte = teinte, taille = 38.dp)
            Column {
                Text(titre, style = TextStyle(fontFamily = SansTexte, fontSize = 17.sp, fontWeight = FontWeight.SemiBold), color = c.texte)
                Text(sous, style = TextStyle(fontFamily = SansTexte, fontSize = 14.sp), color = c.texteSecondaire, maxLines = 1)
            }
        }
    }
}
