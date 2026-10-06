package kaneplarium.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class PackageChangeReceiver(private val onPackageChanged: () -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_PACKAGE_ADDED ||
            action == Intent.ACTION_PACKAGE_REMOVED ||
            action == Intent.ACTION_PACKAGE_CHANGED ||
            action == Intent.ACTION_PACKAGE_REPLACED
        ) {
            onPackageChanged()
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (_: Exception) {
            // Receiver might not be registered
        }
    }
}
