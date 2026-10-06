package kaneplarium.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kaneplarium.launcher.receiver.PackageChangeReceiver
import kaneplarium.launcher.ui.HomeScreen
import kaneplarium.launcher.ui.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private val packageChangeReceiver = PackageChangeReceiver {
        viewModel.reloadAppsFromSystem()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        packageChangeReceiver.register(this)
    }

    override fun onStop() {
        super.onStop()
        packageChangeReceiver.unregister(this)
    }
}

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
    )
}
