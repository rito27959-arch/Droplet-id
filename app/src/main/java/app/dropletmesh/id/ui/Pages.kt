// ============================================================================
// LES BRIQUES DE PAGE — une page Glasmio à grand titre qui se replie, des
// groupes de lignes, et les formats de date. Tous les onglets s'en servent.
// ============================================================================
package app.dropletmesh.id.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.PiedSection
import app.dropletmesh.id.designsystem.TitreSection
import io.github.neoruaa.glasmio.scaffold.GlasmioPinnedListPage
import io.github.neoruaa.glasmio.ui.GlasmioGroupedList
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun Page(
    titre: String,
    sousTitre: String? = null,
    retour: (() -> Unit)? = null,
    avecBarre: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    contenu: LazyListScope.() -> Unit,
) {
    val bas = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (avecBarre) 96.dp else 32.dp
    GlasmioPinnedListPage(
        title = titre,
        subtitle = sousTitre,
        bottomPadding = bas,
        onNavigateBack = retour,
        navigateBackContentDescription = "Retour",
        actions = actions,
        backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
        content = contenu,
    )
}

/** Un groupe : en-tête discret, îlot de lignes, explication dessous. */
fun LazyListScope.groupe(
    cle: String,
    titre: String? = null,
    pied: String? = null,
    lignes: @Composable () -> Unit,
) {
    item(key = cle) {
        Column {
            titre?.let { TitreSection(it) }
            GlasmioGroupedList { lignes() }
            pied?.let { PiedSection(it) }
        }
    }
}

private val zone: ZoneId get() = ZoneId.systemDefault()

fun dateLongue(i: Instant): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE).format(i.atZone(zone))

fun dateHeure(i: Instant): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(Locale.FRANCE).format(i.atZone(zone))

/** « à l'instant », « il y a 5 min », « il y a 3 h », « il y a 2 j », sinon la date. */
fun ilYA(i: Instant, maintenant: Instant = Instant.now()): String {
    val s = (maintenant.epochSecond - i.epochSecond).coerceAtLeast(0)
    return when {
        s < 60 -> "à l'instant"
        s < 3600 -> "il y a ${s / 60} min"
        s < 86_400 -> "il y a ${s / 3600} h"
        s < 30 * 86_400 -> "il y a ${s / 86_400} j"
        else -> "le ${dateLongue(i)}"
    }
}

/** Ce qu'une app reçoit, en mots simples (jamais les noms techniques). */
fun donneesPartagees(portees: List<String>): String =
    if ("profile" in portees) "Un identifiant propre à elle et votre pseudo" else "Un identifiant propre à elle, rien d'autre"
