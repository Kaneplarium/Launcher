package kaneplarium.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kaneplarium.launcher.data.AppModel
import kaneplarium.launcher.data.AppRepository
import kaneplarium.launcher.service.LauncherNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppsUiState {
    data object Loading : AppsUiState
    data class Success(
        val apps: List<AppModel>,
        val isNotificationPermissionGranted: Boolean,
    ) : AppsUiState
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)

    private val _allApps = MutableStateFlow<List<AppModel>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AppsUiState> = combine(
        _allApps,
        LauncherNotificationService.notificationPostTimes,
        _isLoading,
    ) { flowValues ->
        val apps = flowValues[0] as List<AppModel>
        val notificationTimes = flowValues[1] as Map<String, Long>
        val isLoading = flowValues[2] as Boolean

        if (isLoading) {
            AppsUiState.Loading
        } else {
            // Map notification status to apps
            val updatedApps = apps.map { app ->
                val notifTime = notificationTimes[app.packageName]
                val hasNotif = notifTime != null && notifTime > 0L
                app.copy(hasNotification = hasNotif)
            }

            // SORTING: Notification apps at the top (newest first), then alphabetical
            val sortedApps = updatedApps.sortedWith(
                Comparator { a, b ->
                    if (a.hasNotification && !b.hasNotification) {
                        -1
                    } else if (!a.hasNotification && b.hasNotification) {
                        1
                    } else if (a.hasNotification && b.hasNotification) {
                        val timeA = notificationTimes[a.packageName] ?: 0L
                        val timeB = notificationTimes[b.packageName] ?: 0L
                        timeB.compareTo(timeA)
                    } else {
                        a.label.lowercase().compareTo(b.label.lowercase())
                    }
                }
            )

            AppsUiState.Success(
                apps = sortedApps,
                isNotificationPermissionGranted = LauncherNotificationService.isPermissionGranted(getApplication()),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppsUiState.Loading,
    )

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _allApps.value = appRepository.getInstalledApps()
            _isLoading.value = false
        }
    }

    fun reloadAppsFromSystem() {
        viewModelScope.launch {
            appRepository.invalidateCache()
            _isLoading.value = true
            _allApps.value = appRepository.getInstalledApps()
            _isLoading.value = false
        }
    }

    fun launchApp(packageName: String): Boolean {
        return appRepository.launchApp(packageName)
    }

    fun openAppDetails(packageName: String) {
        appRepository.openAppDetails(packageName)
    }

    fun uninstallApp(packageName: String) {
        appRepository.uninstallApp(packageName)
    }

    fun openNotificationSettings() {
        LauncherNotificationService.openNotificationAccessSettings(getApplication())
    }
}
