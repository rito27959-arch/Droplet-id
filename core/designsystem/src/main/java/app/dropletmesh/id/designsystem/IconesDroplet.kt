// ============================================================================
// LES ICÔNES — Droplet ID n'embarque aucune police Apple (SF Symbols n'est
// pas redistribuable). Glasmio passe par un « résolveur » : on lui donne nos
// noms et des icônes Material (Apache 2.0), au trait arrondi.
//
// Les onglets de Glasmio prennent un SfSymbol (une énumération fermée). Pour
// nos onglets, on réutilise quatre de ses noms et on les résout vers nos
// propres icônes ; la table ONGLETS ci-dessous dit lequel sert à quoi.
// ============================================================================
package app.dropletmesh.id.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.neoruaa.glasmio.icons.GlasmioIconResolver
import io.github.neoruaa.glasmio.icons.MaterialGlasmioIconResolver
import io.github.neoruaa.glasmio.icons.SfSymbol

/** Les symboles Glasmio prêtés à nos onglets (résolus vers nos icônes). */
object Onglets {
    val accueil = SfSymbol.House
    val identite = SfSymbol.PersonFilled
    val securite = SfSymbol.Layers // → bouclier
    val acces = SfSymbol.Safari // → applications
    val compte = SfSymbol.Gearshape
}

/** Nos noms d'icônes, à passer à `systemName` / `GlasmioIcon`. */
object Icone {
    const val IDENTITE = "droplet.identite"
    const val CARTE = "droplet.carte"
    const val QR = "droplet.qr"
    const val SCANNER = "droplet.scanner"
    const val VERIFIE = "droplet.verifie"
    const val SCEAU = "droplet.sceau"
    const val BOUCLIER = "droplet.bouclier"
    const val PASSKEY = "droplet.passkey"
    const val MOT_DE_PASSE = "droplet.motdepasse"
    const val DOUBLE_FACTEUR = "droplet.doublefacteur"
    const val TELEPHONE = "droplet.telephone"
    const val TABLETTE = "droplet.tablette"
    const val ORDINATEUR = "droplet.ordinateur"
    const val NAVIGATEUR = "droplet.navigateur"
    const val HISTORIQUE = "droplet.historique"
    const val RECUPERATION = "droplet.recuperation"
    const val APPS = "droplet.apps"
    const val CONFIDENTIALITE = "droplet.confidentialite"
    const val NOTIFICATIONS = "droplet.notifications"
    const val LANGUE = "droplet.langue"
    const val COMPTE = "droplet.compte"
    const val EMAIL = "droplet.email"
    const val NUMERO = "droplet.numero"
    const val NAISSANCE = "droplet.naissance"
    const val PERSONNE = "droplet.personne"
    const val COPIER = "droplet.copier"
    const val EXPORT = "droplet.export"
    const val SUPPRIMER = "droplet.supprimer"
    const val DECONNEXION = "droplet.deconnexion"
    const val BLOQUER = "droplet.bloquer"
    const val ALERTE = "droplet.alerte"
    const val OK = "droplet.ok"
    const val VISIBILITE = "droplet.visibilite"
    const val SECURITE = "droplet.securite"
}

object IconesDroplet : GlasmioIconResolver {
    private val table: Map<String, ImageVector> = mapOf(
        // Onglets (voir Onglets).
        SfSymbol.House.systemName to Icons.Rounded.Home,
        SfSymbol.HouseFilled.systemName to Icons.Rounded.Home,
        SfSymbol.PersonFilled.systemName to Icons.Rounded.Badge,
        SfSymbol.Layers.systemName to Icons.Rounded.Shield,
        SfSymbol.Safari.systemName to Icons.Rounded.Apps,
        SfSymbol.Gearshape.systemName to Icons.Rounded.Settings,
        // Les nôtres.
        Icone.IDENTITE to Icons.Rounded.Badge,
        Icone.CARTE to Icons.Rounded.CreditCard,
        Icone.QR to Icons.Rounded.QrCode2,
        Icone.SCANNER to Icons.Rounded.QrCodeScanner,
        Icone.VERIFIE to Icons.Rounded.Verified,
        Icone.SCEAU to Icons.Rounded.VerifiedUser,
        Icone.BOUCLIER to Icons.Rounded.Shield,
        Icone.PASSKEY to Icons.Rounded.Fingerprint,
        Icone.MOT_DE_PASSE to Icons.Rounded.Password,
        Icone.DOUBLE_FACTEUR to Icons.Rounded.Key,
        Icone.TELEPHONE to Icons.Rounded.PhoneAndroid,
        Icone.TABLETTE to Icons.Rounded.Tablet,
        Icone.ORDINATEUR to Icons.Rounded.Computer,
        Icone.NAVIGATEUR to Icons.Rounded.Public,
        Icone.HISTORIQUE to Icons.Rounded.History,
        Icone.RECUPERATION to Icons.Rounded.LockReset,
        Icone.APPS to Icons.Rounded.Apps,
        Icone.CONFIDENTIALITE to Icons.Rounded.PrivacyTip,
        Icone.NOTIFICATIONS to Icons.Rounded.Notifications,
        Icone.LANGUE to Icons.Rounded.Translate,
        Icone.COMPTE to Icons.Rounded.Settings,
        Icone.EMAIL to Icons.Rounded.Email,
        Icone.NUMERO to Icons.Rounded.Phone,
        Icone.NAISSANCE to Icons.Rounded.Cake,
        Icone.PERSONNE to Icons.Rounded.Person,
        Icone.COPIER to Icons.Rounded.ContentCopy,
        Icone.EXPORT to Icons.Rounded.Download,
        Icone.SUPPRIMER to Icons.Rounded.Delete,
        Icone.DECONNEXION to Icons.AutoMirrored.Rounded.Logout,
        Icone.BLOQUER to Icons.Rounded.Block,
        Icone.ALERTE to Icons.Rounded.Warning,
        Icone.OK to Icons.Rounded.CheckCircle,
        Icone.VISIBILITE to Icons.Rounded.Visibility,
        Icone.SECURITE to Icons.Rounded.Lock,
        "globe" to Icons.Rounded.Language,
    )

    override fun resolve(systemName: String): ImageVector =
        table[systemName] ?: MaterialGlasmioIconResolver.resolve(systemName)
}
