package com.qinmu.eyecare.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qinmu.eyecare.QinMuApplication
import com.qinmu.eyecare.data.model.RemindMode
import com.qinmu.eyecare.data.model.RestSoundEffect
import com.qinmu.eyecare.data.model.SpecialMode
import com.qinmu.eyecare.data.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as QinMuApplication).preferencesRepository

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    fun setRemindMode(mode: RemindMode) {
        viewModelScope.launch {
            repository.updateRemindMode(mode)
        }
    }

    fun setSoundEffect(soundEffect: RestSoundEffect) {
        viewModelScope.launch {
            repository.updateSoundEffect(soundEffect)
        }
    }

    fun setRemindInterval(minutes: Int) {
        val safeMinutes = minutes.coerceIn(1, 180)
        viewModelScope.launch {
            repository.updateRemindInterval(safeMinutes)
        }
    }

    fun setRestDuration(seconds: Int) {
        val safeSeconds = seconds.coerceIn(5, 600)
        viewModelScope.launch {
            repository.updateRestDuration(safeSeconds)
        }
    }

    fun setDualCycleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDualCycleEnabled(enabled)
        }
    }

    fun setDaQinCycleCount(count: Int) {
        val safeCount = count.coerceIn(1, 10)
        viewModelScope.launch {
            repository.updateDaQinCycleCount(safeCount)
        }
    }

    fun setDaQinRestSeconds(seconds: Int) {
        val safeSeconds = seconds.coerceIn(30, 1800)
        viewModelScope.launch {
            repository.updateDaQinRestSeconds(safeSeconds)
        }
    }

    fun setManualSpecialMode(mode: SpecialMode) {
        viewModelScope.launch {
            repository.updateManualSpecialMode(mode)
        }
    }

    fun setAutoGameModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoGameModeEnabled(enabled)
        }
    }

    fun setAutoMeetingModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoMeetingModeEnabled(enabled)
        }
    }

    fun setKeepAliveEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateKeepAliveEnabled(enabled)
            if (enabled) {
                com.qinmu.eyecare.service.KeepAliveWorker.scheduleKeepAliveWork(getApplication())
            } else {
                com.qinmu.eyecare.service.KeepAliveWorker.cancelKeepAliveWork(getApplication())
            }
        }
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoStartEnabled(enabled)
            if (!enabled) {
                com.qinmu.eyecare.service.KeepAliveWorker.cancelKeepAliveWork(getApplication())
            }
        }
    }

    fun saveXiaoQinBackground(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(context.filesDir, "xiaoqin_bg_custom.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                repository.updateXiaoQinBgUri(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveDaQinBackground(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(context.filesDir, "daqin_bg_custom.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                repository.updateDaQinBgUri(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearXiaoQinBackground(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val file = java.io.File(context.filesDir, "xiaoqin_bg_custom.jpg")
            if (file.exists()) file.delete()
            repository.updateXiaoQinBgUri(null)
        }
    }

    fun clearDaQinBackground(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val file = java.io.File(context.filesDir, "daqin_bg_custom.jpg")
            if (file.exists()) file.delete()
            repository.updateDaQinBgUri(null)
        }
    }
}
