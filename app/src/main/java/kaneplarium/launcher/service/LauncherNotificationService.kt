package kaneplarium.launcher.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherNotificationService : NotificationListenerService() {

    companion object {
        // Maps packageName -> latest notification post time in milliseconds
        private val _notificationPostTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
        val notificationPostTimes: StateFlow<Map<String, Long>> = _notificationPostTimes.asStateFlow()

        fun isPermissionGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return (flat != null) && flat.contains(packageName)
        }

        fun openNotificationAccessSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        updateActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveNotifications()
    }

    private fun updateActiveNotifications() {
        try {
            val activeSbns = activeNotifications ?: return
            val map = HashMap<String, Long>()
            for (sbn in activeSbns) {
                val pkg = sbn.packageName ?: continue
                val time = sbn.postTime
                val existing = map[pkg] ?: 0L
                if (time > existing) {
                    map[pkg] = time
                }
            }
            // Only emit state update if map contents actually changed to save battery & CPU cycles
            if (_notificationPostTimes.value != map) {
                _notificationPostTimes.value = map
            }
        } catch (_: Exception) {
            // Ignore if service temporarily disconnects
        }
    }
}
