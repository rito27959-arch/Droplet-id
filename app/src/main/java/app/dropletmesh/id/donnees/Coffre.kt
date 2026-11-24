// ============================================================================
// LE COFFRE — ce que Droplet ID garde sur ce téléphone, chiffré.
//
// Une clé AES-256-GCM (Tink) chiffre chaque valeur ; elle-même est chiffrée
// par une clé maîtresse du Keystore Android, qui ne sort jamais du matériel
// sécurisé. Chaque valeur est liée à son nom (données associées) : on ne
// peut pas échanger deux valeurs chiffrées entre elles.
//
// Rien de tout cela ne part dans les sauvegardes (voir regles_sauvegarde.xml).
// ============================================================================
package app.dropletmesh.id.donnees

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.util.Base64

class Coffre(contexte: Context) {
    private val app = contexte.applicationContext
    private val prefs = app.getSharedPreferences("droplet_id_coffre", Context.MODE_PRIVATE)

    private val aead: Aead by lazy {
        AeadConfig.register()
        @Suppress("DEPRECATION")
        AndroidKeysetManager.Builder()
            .withSharedPref(app, "jeu_de_cles", "droplet_id_tink")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://droplet_id_cle_maitresse")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    fun ecrire(nom: String, valeur: ByteArray) {
        val chiffre = aead.encrypt(valeur, nom.toByteArray(Charsets.UTF_8))
        prefs.edit().putString(nom, Base64.getEncoder().encodeToString(chiffre)).commit()
    }

    fun ecrireTexte(nom: String, valeur: String) = ecrire(nom, valeur.toByteArray(Charsets.UTF_8))

    fun lire(nom: String): ByteArray? {
        val brut = prefs.getString(nom, null) ?: return null
        return aead.decrypt(Base64.getDecoder().decode(brut), nom.toByteArray(Charsets.UTF_8))
    }

    fun lireTexte(nom: String): String? = lire(nom)?.toString(Charsets.UTF_8)

    fun contient(nom: String): Boolean = prefs.contains(nom)

    fun effacer(vararg noms: String) {
        prefs.edit().apply { noms.forEach(::remove) }.commit()
    }
}
