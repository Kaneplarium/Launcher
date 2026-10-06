package kaneplarium.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_preferences")

data class LauncherSettings(
    val gridColumns: Int = 4,
    val iconSizeDp: Int = 56,
    val showAppLabels: Boolean = true,
    val pinnedApps: Set<String> = emptySet(),
    val dockApps: Set<String> = emptySet(),
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
        val SHOW_APP_LABELS = booleanPreferencesKey("show_app_labels")
        val PINNED_APPS = stringSetPreferencesKey("pinned_apps")
        val DOCK_APPS = stringSetPreferencesKey("dock_apps")
    }

    val launcherSettings: Flow<LauncherSettings> = context.dataStore.data.map { preferences ->
        LauncherSettings(
            gridColumns = preferences[PreferencesKeys.GRID_COLUMNS] ?: 4,
            iconSizeDp = preferences[PreferencesKeys.ICON_SIZE_DP] ?: 56,
            showAppLabels = preferences[PreferencesKeys.SHOW_APP_LABELS] ?: true,
            pinnedApps = preferences[PreferencesKeys.PINNED_APPS] ?: emptySet(),
            dockApps = preferences[PreferencesKeys.DOCK_APPS] ?: emptySet(),
        )
    }

    suspend fun updateGridColumns(columns: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_COLUMNS] = columns
        }
    }

    suspend fun updateIconSizeDp(sizeDp: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ICON_SIZE_DP] = sizeDp
        }
    }

    suspend fun updateShowAppLabels(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_APP_LABELS] = show
        }
    }

    suspend fun togglePinAppToHomescreen(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PINNED_APPS] ?: emptySet()
            if (current.contains(packageName)) {
                preferences[PreferencesKeys.PINNED_APPS] = current - packageName
            } else {
                preferences[PreferencesKeys.PINNED_APPS] = current + packageName
            }
        }
    }

    suspend fun togglePinAppToDock(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.DOCK_APPS] ?: emptySet()
            if (current.contains(packageName)) {
                preferences[PreferencesKeys.DOCK_APPS] = current - packageName
            } else {
                // Limit dock apps to max 5
                if (current.size < 5) {
                    preferences[PreferencesKeys.DOCK_APPS] = current + packageName
                }
            }
        }
    }
}
