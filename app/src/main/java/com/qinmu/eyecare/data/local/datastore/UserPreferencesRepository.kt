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
import com.qinmu.eyecare.data.model.SpecialMode
import com.qinmu.eyecare.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qinmu_user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val REMIND_INTERVAL_MINUTES = intPreferencesKey("remind_interval_minutes")
        val REST_DURATION_SECONDS = intPreferencesKey("rest_duration_seconds")
        val DA_QIN_CYCLE_COUNT = intPreferencesKey("da_qin_cycle_count")
        val DA_QIN_REST_SECONDS = intPreferencesKey("da_qin_rest_seconds")
        val IS_DUAL_CYCLE_ENABLED = booleanPreferencesKey("is_dual_cycle_enabled")
        val MANUAL_SPECIAL_MODE = stringPreferencesKey("manual_special_mode")
        val IS_AUTO_GAME_MODE_ENABLED = booleanPreferencesKey("is_auto_game_mode_enabled")
        val IS_AUTO_MEETING_MODE_ENABLED = booleanPreferencesKey("is_auto_meeting_mode_enabled")
        val REMIND_MODE = stringPreferencesKey("remind_mode")
        val SOUND_EFFECT = stringPreferencesKey("sound_effect")
        val IS_KEEP_ALIVE_ENABLED = booleanPreferencesKey("is_keep_alive_enabled")
        val XIAO_QIN_BG_URI = stringPreferencesKey("xiao_qin_bg_uri")
        val DA_QIN_BG_URI = stringPreferencesKey("da_qin_bg_uri")
        val SAVED_SCREEN_SECONDS = longPreferencesKey("saved_screen_seconds")
        val SAVED_LAST_ACTIVE_TIME = longPreferencesKey("saved_last_active_time")
        val SAVED_XIAO_QIN_COMPLETED_COUNT = intPreferencesKey("saved_xiao_qin_completed_count")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val interval = prefs[PreferenceKeys.REMIND_INTERVAL_MINUTES] ?: 20
        val duration = prefs[PreferenceKeys.REST_DURATION_SECONDS] ?: 20
        val daQinCycle = prefs[PreferenceKeys.DA_QIN_CYCLE_COUNT] ?: 3
        val daQinRest = prefs[PreferenceKeys.DA_QIN_REST_SECONDS] ?: 180
        val isDualCycle = prefs[PreferenceKeys.IS_DUAL_CYCLE_ENABLED] ?: true
        
        val specialModeStr = prefs[PreferenceKeys.MANUAL_SPECIAL_MODE] ?: SpecialMode.NONE.name
        val manualMode = try {
            SpecialMode.valueOf(specialModeStr)
        } catch (e: Exception) {
            SpecialMode.NONE
        }

        val autoGame = prefs[PreferenceKeys.IS_AUTO_GAME_MODE_ENABLED] ?: true
        val autoMeeting = prefs[PreferenceKeys.IS_AUTO_MEETING_MODE_ENABLED] ?: true

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

        val isKeepAlive = prefs[PreferenceKeys.IS_KEEP_ALIVE_ENABLED] ?: true
        val xiaoQinBg = prefs[PreferenceKeys.XIAO_QIN_BG_URI]
        val daQinBg = prefs[PreferenceKeys.DA_QIN_BG_URI]

        UserPreferences(
            remindIntervalMinutes = interval,
            restDurationSeconds = duration,
            daQinCycleCount = daQinCycle,
            daQinRestSeconds = daQinRest,
            isDualCycleEnabled = isDualCycle,
            manualSpecialMode = manualMode,
            isAutoGameModeEnabled = autoGame,
            isAutoMeetingModeEnabled = autoMeeting,
            remindMode = mode,
            soundEffect = sound,
            isKeepAliveEnabled = isKeepAlive,
            xiaoQinBgUri = xiaoQinBg,
            daQinBgUri = daQinBg
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

    suspend fun updateDaQinCycleCount(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DA_QIN_CYCLE_COUNT] = count
        }
    }

    suspend fun updateDaQinRestSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DA_QIN_REST_SECONDS] = seconds
        }
    }

    suspend fun updateDualCycleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DUAL_CYCLE_ENABLED] = enabled
        }
    }

    suspend fun updateManualSpecialMode(mode: SpecialMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.MANUAL_SPECIAL_MODE] = mode.name
        }
    }

    suspend fun updateAutoGameModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_AUTO_GAME_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateAutoMeetingModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_AUTO_MEETING_MODE_ENABLED] = enabled
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

    suspend fun updateKeepAliveEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_KEEP_ALIVE_ENABLED] = enabled
        }
    }

    suspend fun updateXiaoQinBgUri(path: String?) {
        context.dataStore.edit { prefs ->
            if (path != null) {
                prefs[PreferenceKeys.XIAO_QIN_BG_URI] = path
            } else {
                prefs.remove(PreferenceKeys.XIAO_QIN_BG_URI)
            }
        }
    }

    suspend fun updateDaQinBgUri(path: String?) {
        context.dataStore.edit { prefs ->
            if (path != null) {
                prefs[PreferenceKeys.DA_QIN_BG_URI] = path
            } else {
                prefs.remove(PreferenceKeys.DA_QIN_BG_URI)
            }
        }
    }

    data class SavedTimerState(
        val screenSeconds: Long = 0L,
        val lastActiveTimeMs: Long = 0L,
        val xiaoQinCompletedCount: Int = 0
    )

    suspend fun getSavedTimerState(): SavedTimerState {
        return context.dataStore.data.map { prefs ->
            SavedTimerState(
                screenSeconds = prefs[PreferenceKeys.SAVED_SCREEN_SECONDS] ?: 0L,
                lastActiveTimeMs = prefs[PreferenceKeys.SAVED_LAST_ACTIVE_TIME] ?: 0L,
                xiaoQinCompletedCount = prefs[PreferenceKeys.SAVED_XIAO_QIN_COMPLETED_COUNT] ?: 0
            )
        }.first()
    }

    suspend fun saveTimerState(screenSeconds: Long, lastActiveTimeMs: Long, xiaoQinCompletedCount: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SAVED_SCREEN_SECONDS] = screenSeconds
            prefs[PreferenceKeys.SAVED_LAST_ACTIVE_TIME] = lastActiveTimeMs
            prefs[PreferenceKeys.SAVED_XIAO_QIN_COMPLETED_COUNT] = xiaoQinCompletedCount
        }
    }

    suspend fun clearSavedTimerState() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.SAVED_SCREEN_SECONDS)
            prefs.remove(PreferenceKeys.SAVED_LAST_ACTIVE_TIME)
            prefs.remove(PreferenceKeys.SAVED_XIAO_QIN_COMPLETED_COUNT)
        }
    }
}
