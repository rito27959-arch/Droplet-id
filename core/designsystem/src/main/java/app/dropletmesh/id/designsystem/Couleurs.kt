// ============================================================================
// ENCRE ET LAITON — la palette de Droplet ID (voir le dossier de conception,
// section Direction artistique). Contrastes calculés selon WCAG 2.2.
//
//   encre   : les actions (bouton principal, interrupteurs, onglet actif)
//   laiton  : l'autorité (sceau « vérifié », carte d'identité, focus)
//   rouge   : seulement les erreurs, alertes et actions irréversibles
//
// Le laiton ne va JAMAIS sur un bouton ni dans un paragraphe : s'il est
// partout, il ne certifie plus rien.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CouleursDroplet(
    val sombre: Boolean,
    /** Fond d'écran : blanc cassé / noir profond. */
    val fond: Color,
    /** Cartes et listes. */
    val surface: Color,
    /** Feuilles et dialogues. */
    val surfaceElevee: Color,
    val texte: Color,
    /** 5,9:1 sur fond clair, 7,3:1 sur fond sombre. */
    val texteSecondaire: Color,
    /** Décorative seulement (contraste < 3:1). */
    val bordure: Color,
    /** Bouton principal. 16,3:1 / 16,5:1 avec [surEncre]. */
    val encre: Color,
    val surEncre: Color,
    /** Sceau, filet et guilloché de la carte, focus. 5,7:1 / 9,9:1. */
    val laiton: Color,
    val surLaiton: Color,
    /** Une action a réussi (à ne pas confondre avec « vérifié », en laiton). */
    val succes: Color,
    val avertissement: Color,
    /** Erreurs, actions destructives, pastille « action requise ». */
    val erreur: Color,
)

/** Le noir de la carte d'identité : identique dans les deux modes. */
val NoirCarte = Color(0xFF0A0A0B)

/** Le laiton sur la carte noire (9,9:1), quel que soit le mode. */
val LaitonCarte = Color(0xFFD6B35F)

val CouleursClaires = CouleursDroplet(
    sombre = false,
    fond = Color(0xFFF6F5F2),
    surface = Color(0xFFFFFFFF),
    surfaceElevee = Color(0xFFFFFFFF),
    texte = Color(0xFF141416),
    texteSecondaire = Color(0xFF5E5E66),
    bordure = Color(0xFFD9D8D4),
    encre = Color(0xFF17181C),
    surEncre = Color(0xFFF6F5F2),
    laiton = Color(0xFF7A5C14),
    surLaiton = Color(0xFFFFFFFF),
    succes = Color(0xFF1E7A46),
    avertissement = Color(0xFF8A5A00),
    erreur = Color(0xFFC8102E),
)

val CouleursSombres = CouleursDroplet(
    sombre = true,
    fond = Color(0xFF0A0A0B),
    surface = Color(0xFF161618),
    surfaceElevee = Color(0xFF1F1F22),
    texte = Color(0xFFF2F1EE),
    texteSecondaire = Color(0xFF9C9CA3),
    bordure = Color(0xFF2A2A2E),
    encre = Color(0xFFECEAE4),
    surEncre = Color(0xFF0A0A0B),
    laiton = Color(0xFFD6B35F),
    surLaiton = Color(0xFF0A0A0B),
    succes = Color(0xFF4CC38A),
    avertissement = Color(0xFFF0B429),
    erreur = Color(0xFFFF4D5E),
)

val LocalCouleursDroplet = staticCompositionLocalOf { CouleursClaires }
