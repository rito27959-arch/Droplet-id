// ============================================================================
// L'ACTIVITÉ UNIQUE — tout l'écran est en Compose (Glasmio).
// AppCompatActivity : nécessaire à BiometricPrompt (FragmentActivity).
// ============================================================================
package app.dropletmesh.id

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.dropletmesh.id.donnees.LienDemande
import app.dropletmesh.id.donnees.Liens
import app.dropletmesh.id.ui.Racine

class MainActivity : AppCompatActivity() {

    private val etat: EtatApp by viewModels()
    private var lienEntrant by mutableStateOf<LienDemande?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) lienEntrant = lienDe(intent)
        setContent {
            Racine(
                etat = etat,
                lienEntrant = lienEntrant,
                lienTraite = { lienEntrant = null },
                // La décision est prise : la page de connexion attend dans le
                // navigateur, on la laisse revenir devant.
                revenirAuNavigateur = { moveTaskToBack(true) },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lienDe(intent)?.let { lienEntrant = it }
    }

    override fun onResume() {
        super.onResume()
        // L'utilisateur a pu activer son verrouillage dans les réglages.
        etat.rafraichirAppareil()
    }

    private fun lienDe(intent: Intent?): LienDemande? =
        intent?.takeIf { it.action == Intent.ACTION_VIEW }?.dataString?.let(Liens::demande)
}
