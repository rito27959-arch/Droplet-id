package app.dropletmesh.id

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.dropletmesh.id.designsystem.Droplet
import app.dropletmesh.id.designsystem.ThemeDroplet
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeDroplet {
                GlasmioBackdropHost(sampledColor = Droplet.couleurs.fond, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Droplet ID") }
                }
            }
        }
    }
}
