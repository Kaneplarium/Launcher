package kaneplarium.launcher.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class AppModel(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap,
    val hasNotification: Boolean = false,
)
