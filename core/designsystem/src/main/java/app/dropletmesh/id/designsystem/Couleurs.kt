// ============================================================================
// LES COULEURS — celles du logo et du site de Droplet ID.
//
//   Noir profond pour le fond, graphite pour les surfaces, argent pour le
//   texte et l'action (bouton en métal, texte noir dessus). Le rouge du bouton
//   de la boucle n'apparaît que pour ce qui retire ou alerte.
//   Le mode clair garde la même logique : papier, encre graphite, rouge rare.
//
// Les noms des champs restent ceux de l'app : « encre » = la couleur d'action.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class CouleursDroplet(
    val sombre: Boolean,
    /** Fond des écrans. */
    val fond: Color,
    /** Groupes de lignes, cartes. */
    val surface: Color,
    /** Une surface sur une surface (pastille, champ, feuille). */
    val surfaceElevee: Color,
    /** Texte principal : 4,5:1 minimum partout. */
    val texte: Color,
    /** Sous-titres et valeurs. */
    val texteSecondaire: Color,
    /** Chevrons, traits : décoratif. */
    val bordure: Color,
    /** Couleur d'action (bouton principal, interrupteurs, liens). */
    val encre: Color,
    /** Le texte posé sur la couleur d'action. */
    val surEncre: Color,
    /** Le rouge du bouton de la boucle : retirer, alerter. Jamais décoratif. */
    val goutte: Color,
    val succes: Color,
    val avertissement: Color,
    val erreur: Color,
)

/** Le noir de la scène (ouverture, présentation) : le même en clair et en sombre. */
val NoirScene = Color(0xFF050506)
val GraphiteScene = Color(0xFF1B1C20)
val ArgentScene = Color(0xFFE6E8EB)
val ArgentScene2 = Color(0xFFA4A8AF)
val RougeBoucle = Color(0xFFE03A2F)

/** Le métal des boutons principaux (comme sur le site). */
val Metal = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFC3C6CC), Color(0xFF8E939A)))

/** Ancien nom de la couleur de marque, gardé pour les écrans existants. */
val Goutte = RougeBoucle
val GoutteRemplie = Color(0xFFC92A20)

val CouleursClaires = CouleursDroplet(
    sombre = false,
    fond = Color(0xFFF2F2F4),
    surface = Color(0xFFFFFFFF),
    surfaceElevee = Color(0xFFEDEDF0),
    texte = Color(0xFF0E0F12),
    texteSecondaire = Color(0xFF5C6068),
    bordure = Color(0xFFC4C6CB),
    encre = Color(0xFF1B1C20),
    surEncre = Color(0xFFFFFFFF),
    goutte = Color(0xFFC92A20),
    succes = Color(0xFF107A43),
    avertissement = Color(0xFF9F5300),
    erreur = Color(0xFFC92A20),
)

val CouleursSombres = CouleursDroplet(
    sombre = true,
    fond = NoirScene,
    surface = Color(0xFF141518),
    surfaceElevee = Color(0xFF1F2024),
    texte = ArgentScene,
    texteSecondaire = ArgentScene2,
    bordure = Color(0xFF55585E),
    encre = ArgentScene,
    surEncre = Color(0xFF0E0F12),
    goutte = RougeBoucle,
    succes = Color(0xFF30D158),
    avertissement = Color(0xFFFF9F0A),
    erreur = Color(0xFFFF5A4F),
)

val LocalCouleursDroplet = staticCompositionLocalOf { CouleursClaires }
