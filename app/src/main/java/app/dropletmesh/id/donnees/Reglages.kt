// Les réglages non sensibles (apparence, présentation déjà vue). Rien de
// secret ici : le coffre chiffré garde la clé et les identifiants.
package app.dropletmesh.id.donnees

import android.content.Context

enum class Apparence(val titre: String) { Systeme("Comme le système"), Clair("Clair"), Sombre("Sombre") }

class Reglages(contexte: Context) {
    private val prefs = contexte.applicationContext.getSharedPreferences("droplet_id_reglages", Context.MODE_PRIVATE)

    var presentationVue: Boolean
        get() = prefs.getBoolean("presentation_vue", false)
        set(v) = prefs.edit().putBoolean("presentation_vue", v).apply()

    var apparence: Apparence
        get() = runCatching { Apparence.valueOf(prefs.getString("apparence", null) ?: "") }.getOrDefault(Apparence.Systeme)
        set(v) = prefs.edit().putString("apparence", v.name).apply()

    var reduireTransparence: Boolean
        get() = prefs.getBoolean("reduire_transparence", false)
        set(v) = prefs.edit().putBoolean("reduire_transparence", v).apply()
}
