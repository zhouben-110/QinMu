package com.qinmu.eyecare.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qinmu.eyecare.data.model.RemindMode
import com.qinmu.eyecare.data.model.RestSoundEffect
import com.qinmu.eyecare.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qinmu_user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val REMIND_INTERVAL_MINUTES = intPreferencesKey("remind_interval_minutes")
        val REST_DURATION_SECONDS = intPreferencesKey("rest_duration_seconds")
        val REMIND_MODE = stringPreferencesKey("remind_mode")
        val SOUND_EFFECT = stringPreferencesKey("sound_effect")
        val IS_FILTER_ENABLED = booleanPreferencesKey("is_filter_enabled")
        val FILTER_COLOR_ARGB = longPreferencesKey("filter_color_argb")
        val FILTER_ALPHA = floatPreferencesKey("filter_alpha")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val interval = prefs[PreferenceKeys.REMIND_INTERVAL_MINUTES] ?: 20
        val duration = prefs[PreferenceKeys.REST_DURATION_SECONDS] ?: 20
        val modeStr = prefs[PreferenceKeys.REMIND_MODE] ?: RemindMode.NOTIFICATION.name
        val mode = try {
            RemindMode.valueOf(modeStr)
        } catch (e: Exception) {
            RemindMode.NOTIFICATION
        }

        val soundStr = prefs[PreferenceKeys.SOUND_EFFECT] ?: RestSoundEffect.SYSTEM_NOTIFICATION.name
        val sound = try {
            RestSoundEffect.valueOf(soundStr)
        } catch (e: Exception) {
            RestSoundEffect.SYSTEM_NOTIFICATION
        }

        val filterEnabled = prefs[PreferenceKeys.IS_FILTER_ENABLED] ?: false
        val colorArgb = prefs[PreferenceKeys.FILTER_COLOR_ARGB] ?: 0x33FFB74D
        val alpha = prefs[PreferenceKeys.FILTER_ALPHA] ?: 0.2f

        UserPreferences(
            remindIntervalMinutes = interval,
            restDurationSeconds = duration,
            remindMode = mode,
            soundEffect = sound,
            isFilterEnabled = filterEnabled,
            filterColorArgb = colorArgb,
            filterAlpha = alpha
        )
    }

    suspend fun updateRemindInterval(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.REMIND_INTERVAL_MINUTES] = minutes
        }
    }

    suspend fun updateRestDuration(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.REST_DURATION_SECONDS] = seconds
        }
    }

    suspend fun updateRemindMode(mode: RemindMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.REMIND_MODE] = mode.name
        }
    }

    suspend fun updateSoundEffect(soundEffect: RestSoundEffect) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SOUND_EFFECT] = soundEffect.name
        }
    }

    suspend fun updateFilterEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_FILTER_ENABLED] = enabled
        }
    }

    suspend fun updateFilterSettings(colorArgb: Long, alpha: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.FILTER_COLOR_ARGB] = colorArgb
            prefs[PreferenceKeys.FILTER_ALPHA] = alpha
        }
    }
}
