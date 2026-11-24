// ============================================================================
// CE TÉLÉPHONE — ce qui le protège, lu sur l'appareil lui-même (rien n'est
// envoyé). Sert à l'état de sécurité de l'Accueil et à l'onglet Sécurité.
// ============================================================================
package app.dropletmesh.id.donnees

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG

data class EtatAppareil(
    val modele: String,
    val versionAndroid: String,
    /** Un code, un schéma ou un mot de passe protège l'écran. */
    val verrouEcran: Boolean,
    /** Une empreinte ou un visage « fort » est enregistré. */
    val biometrie: Boolean,
    /** Le téléphone a une puce de sécurité séparée (StrongBox). */
    val strongBox: Boolean,
) {
    /** Les actions à faire, de la plus importante à la moins importante. */
    val actions: List<ActionSecurite>
        get() = buildList {
            if (!verrouEcran) add(ActionSecurite.VerrouEcran)
            if (verrouEcran && !biometrie) add(ActionSecurite.Biometrie)
        }

    companion object {
        fun lire(contexte: Context): EtatAppareil {
            val keyguard = contexte.getSystemService(KeyguardManager::class.java)
            val bio = BiometricManager.from(contexte).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
            return EtatAppareil(
                modele = Build.MODEL ?: "Téléphone",
                versionAndroid = Build.VERSION.RELEASE ?: "",
                verrouEcran = keyguard?.isDeviceSecure == true,
                biometrie = bio,
                strongBox = contexte.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE),
            )
        }
    }
}

enum class ActionSecurite(val titre: String, val explication: String) {
    VerrouEcran(
        "Activez le verrouillage de l'écran",
        "Sans code ni empreinte, quiconque tient ce téléphone peut approuver une connexion à votre place.",
    ),
    Biometrie(
        "Ajoutez une empreinte ou votre visage",
        "Approuver une connexion se fera d'un geste, sans taper votre code.",
    ),
}
