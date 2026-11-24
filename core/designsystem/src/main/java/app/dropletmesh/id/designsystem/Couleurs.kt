// ============================================================================
// LES COULEURS — celles de Droplet Design, la famille Droplet.
//
//   Les gris d'Apple pour tout l'écran (noir pur en sombre, pour l'OLED).
//   Une seule couleur d'action : la goutte. Jamais brute sous du texte
//   blanc : on peint la goutte remplie (4,55:1).
//   Succès, attention, erreur portent un sens, jamais une décoration.
//
// Les noms des champs restent ceux de l'app (encre = la couleur d'action),
// les valeurs viennent de design/droplet-design.json (dépôt droplet-site).
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CouleursDroplet(
    val sombre: Boolean,
    /** `fond-groupe` : le fond gris des écrans en listes groupées. */
    val fond: Color,
    /** `surface` : les groupes de lignes. */
    val surface: Color,
    /** `surface-haute` : une surface sur une surface (pastille, champ). */
    val surfaceElevee: Color,
    /** `texte` : 4,5:1 minimum partout. */
    val texte: Color,
    /** `texte-2` : sous-titres et valeurs. */
    val texteSecondaire: Color,
    /** `texte-3` : chevrons et traits seulement (3:1). */
    val bordure: Color,
    /** `goutte-remplie` : boutons pleins, interrupteurs, liens. */
    val encre: Color,
    /** Le texte posé sur la goutte remplie. */
    val surEncre: Color,
    /** `goutte` : le logo, la lueur, l'avatar. */
    val goutte: Color,
    val succes: Color,
    val avertissement: Color,
    val erreur: Color,
)

/** La goutte, couleur de Droplet. */
val Goutte = Color(0xFFFF2D55)

/** La goutte assombrie juste assez pour porter du texte blanc (4,55:1). */
val GoutteRemplie = Color(0xFFEC002D)

val CouleursClaires = CouleursDroplet(
    sombre = false,
    fond = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surfaceElevee = Color(0xFFF2F2F7),
    texte = Color(0xFF1D1D1F),
    texteSecondaire = Color(0xFF6E6E73),
    bordure = Color(0xFFC4C4C7),
    encre = GoutteRemplie,
    surEncre = Color.White,
    goutte = Goutte,
    succes = Color(0xFF107A43),
    avertissement = Color(0xFF9F5300),
    erreur = Color(0xFFD12B21),
)

val CouleursSombres = CouleursDroplet(
    sombre = true,
    fond = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surfaceElevee = Color(0xFF2C2C2E),
    texte = Color(0xFFF5F5F7),
    texteSecondaire = Color(0xFFA1A1A6),
    bordure = Color(0xFF5A5A5E),
    encre = GoutteRemplie,
    surEncre = Color.White,
    goutte = Color(0xFFFF375F),
    succes = Color(0xFF30D158),
    avertissement = Color(0xFFFF9F0A),
    erreur = Color(0xFFFF453A),
)

val LocalCouleursDroplet = staticCompositionLocalOf { CouleursClaires }
