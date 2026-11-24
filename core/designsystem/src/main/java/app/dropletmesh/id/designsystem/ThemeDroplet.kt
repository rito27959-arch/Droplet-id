// ============================================================================
// LE THÈME — Glasmio pour les contrôles (verre, menus, feuilles, alertes,
// listes, navigation), Encre et laiton pour l'identité.
//
// On ne remplace pas le système de Glasmio : on lui passe nos couleurs
// (accent = encre, destructif = rouge, fonds blanc cassé / noir profond) et
// nos polices. Glasmio garde ses formes, son verre et ses mouvements.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.github.neoruaa.glasmio.core.GlassAppearance
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.core.defaultGlassColors
import io.github.neoruaa.glasmio.core.glasmioColorScheme
import io.github.neoruaa.glasmio.core.glasmioTypography
import io.github.neoruaa.glasmio.icons.GlasmioIconProvider

// La typographie « document officiel » (validée sur les maquettes, option B) :
//   Source Serif 4  — les titres, comme un passeport ;
//   Source Sans 3   — tout le texte de lecture et les contrôles ;
//   Source Code Pro — les identifiants, lus caractère par caractère.
// Trois polices variables SIL OFL 1.1, embarquées : l'app marche hors ligne.

private fun variable(res: Int, poids: Int) = Font(
    res,
    FontWeight(poids),
    variationSettings = FontVariation.Settings(FontVariation.weight(poids)),
)

/** Les titres (écran, section, carte d'identité). */
val SerifTitres = FontFamily(
    variable(R.font.source_serif_4, 500),
    variable(R.font.source_serif_4, 600),
    variable(R.font.source_serif_4, 700),
)

/** Le texte et les contrôles. */
val SansTexte = FontFamily(
    variable(R.font.source_sans_3, 400),
    variable(R.font.source_sans_3, 500),
    variable(R.font.source_sans_3, 600),
    variable(R.font.source_sans_3, 700),
)

/** Les identifiants (ID Droplet, codes). */
val MonoIdentifiants = FontFamily(
    variable(R.font.source_code_pro, 500),
    variable(R.font.source_code_pro, 600),
)

@Composable
fun ThemeDroplet(
    sombre: Boolean = isSystemInDarkTheme(),
    reduireTransparence: Boolean = false,
    content: @Composable () -> Unit,
) {
    val c = if (sombre) CouleursSombres else CouleursClaires
    val glass = defaultGlassColors(sombre, c.encre).copy(
        content = c.texte,
        secondaryContent = c.texteSecondaire,
        groupedBackground = c.fond,
        elevatedBackground = c.surface,
        destructive = c.erreur,
    )
    GlasmioIconProvider(resolver = IconesDroplet) {
        GlasmioTheme(
            isDark = sombre,
            colorScheme = glasmioColorScheme(sombre, c.encre),
            typography = glasmioTypography(SansTexte),
            colors = glass,
            appearance = GlassAppearance(reduceTransparency = reduireTransparence),
        ) {
            CompositionLocalProvider(LocalCouleursDroplet provides c, content = content)
        }
    }
}

object Droplet {
    val couleurs: CouleursDroplet
        @Composable @ReadOnlyComposable get() = LocalCouleursDroplet.current
}
