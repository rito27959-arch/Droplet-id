// ============================================================================
// LE THÈME — Glasmio pour les contrôles (verre, menus, feuilles, alertes,
// listes, navigation), Droplet Design pour l'identité de la famille.
//
// On ne remplace pas le système de Glasmio : on lui passe nos couleurs
// (accent = la goutte, destructif = rouge, gris système d'Apple) et
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

// La typographie de la famille Droplet (Droplet Design) :
//   Inter Tight — les titres, gras et serrés ;
//   Inter       — le texte, les contrôles et les chiffres (à chasse fixe).
// Polices variables SIL OFL 1.1, embarquées : l'app marche hors ligne.

private fun variable(res: Int, poids: Int) = Font(
    res,
    FontWeight(poids),
    variationSettings = FontVariation.Settings(FontVariation.weight(poids)),
)

/** Les titres. */
val TitresDroplet = FontFamily(
    variable(R.font.inter_tight, 600),
    variable(R.font.inter_tight, 700),
)

/** Le texte et les contrôles. */
val TexteDroplet = FontFamily(
    variable(R.font.inter, 400),
    variable(R.font.inter, 500),
    variable(R.font.inter, 600),
    variable(R.font.inter, 700),
)

/** Les identifiants et les codes : Inter, chiffres à chasse fixe (voir [StyleChiffres]). */
val ChiffresDroplet = TexteDroplet

/** Les réglages OpenType des chiffres qui ne doivent pas trembler. */
const val StyleChiffres = "tnum"

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
    // Le texte en Inter ; les grands titres en Inter Tight gras, serrés de
    // −2,2 % comme dans toute la famille.
    val base = glasmioTypography(TexteDroplet)
    fun titre(t: androidx.compose.ui.text.TextStyle) =
        t.copy(fontFamily = TitresDroplet, fontWeight = FontWeight.Bold, letterSpacing = androidx.compose.ui.unit.TextUnit(-0.022f, androidx.compose.ui.unit.TextUnitType.Em))
    val typographie = base.copy(
        displayLarge = titre(base.displayLarge),
        displayMedium = titre(base.displayMedium),
        displaySmall = titre(base.displaySmall),
        headlineLarge = titre(base.headlineLarge),
        headlineMedium = titre(base.headlineMedium),
    )
    GlasmioIconProvider(resolver = IconesDroplet) {
        GlasmioTheme(
            isDark = sombre,
            colorScheme = glasmioColorScheme(sombre, c.encre),
            typography = typographie,
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
