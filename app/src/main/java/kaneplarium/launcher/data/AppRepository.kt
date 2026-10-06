package kaneplarium.launcher.data

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    // Dummy 1x1 bitmap to avoid expensive icon loading during startup
    private val placeholderBitmap = ImageBitmap(1, 1)

    // Cached app list for instant quickstart
    private var cachedApps: List<AppModel>? = null

    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        cachedApps?.let { return@withContext it }

        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        val apps = resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(packageManager).toString()

            AppModel(
                packageName = packageName,
                activityName = resolveInfo.activityInfo.name,
                label = label,
                icon = placeholderBitmap,
            )
        }.sortedBy { it.label.lowercase() }

        cachedApps = apps
        apps
    }

    fun invalidateCache() {
        cachedApps = null
    }

    fun launchApp(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }

    fun openAppDetails(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
