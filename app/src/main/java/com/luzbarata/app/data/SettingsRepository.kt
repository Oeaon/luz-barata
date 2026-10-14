package com.luzbarata.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.luzbarata.core.PvpcZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val zone: PvpcZone = PvpcZone.PENINSULA,
    /** Duración de la franja buscada (lavadora, lavavajillas…), de 1 a 4 horas. */
    val windowHours: Int = 2,
    val notifyTomorrow: Boolean = false,
    val lastNotifiedDate: LocalDate? = null,
)

class SettingsRepository(private val store: DataStore<Preferences>) {
    val settings: Flow<AppSettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            AppSettings(
                zone = PvpcZone.entries.firstOrNull { it.name == prefs[ZONE] } ?: PvpcZone.PENINSULA,
                windowHours = (prefs[WINDOW_HOURS] ?: 2).coerceIn(MIN_WINDOW, MAX_WINDOW),
                notifyTomorrow = prefs[NOTIFY_TOMORROW] ?: false,
                lastNotifiedDate = prefs[LAST_NOTIFIED]?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            )
        }

    suspend fun setZone(zone: PvpcZone) {
        store.edit { it[ZONE] = zone.name }
    }

    suspend fun setWindowHours(hours: Int) {
        store.edit { it[WINDOW_HOURS] = hours.coerceIn(MIN_WINDOW, MAX_WINDOW) }
    }

    suspend fun setNotifyTomorrow(enabled: Boolean) {
        store.edit { it[NOTIFY_TOMORROW] = enabled }
    }

    suspend fun setLastNotified(date: LocalDate) {
        store.edit { it[LAST_NOTIFIED] = date.toString() }
    }

    companion object {
        const val MIN_WINDOW = 1
        const val MAX_WINDOW = 4

        private val ZONE = stringPreferencesKey("zone")
        private val WINDOW_HOURS = intPreferencesKey("window_hours")
        private val NOTIFY_TOMORROW = booleanPreferencesKey("notify_tomorrow")
        private val LAST_NOTIFIED = stringPreferencesKey("last_notified")
    }
}
