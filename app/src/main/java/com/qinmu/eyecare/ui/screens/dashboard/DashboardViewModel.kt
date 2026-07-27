package com.qinmu.eyecare.ui.screens.dashboard

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qinmu.eyecare.QinMuApplication
import com.qinmu.eyecare.data.model.SpecialMode
import com.qinmu.eyecare.data.model.UserPreferences
import com.qinmu.eyecare.service.EyeProtectionService
import com.qinmu.eyecare.util.PermissionUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as QinMuApplication).preferencesRepository

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val currentScreenSeconds: StateFlow<Long> = EyeProtectionService.currentScreenSeconds
    val isPaused: StateFlow<Boolean> = EyeProtectionService.isPaused
    val xiaoQinCompletedCount: StateFlow<Int> = EyeProtectionService.xiaoQinCompletedCount
    val effectiveSpecialMode: StateFlow<SpecialMode> = EyeProtectionService.effectiveSpecialMode

    fun setManualSpecialMode(mode: SpecialMode) {
        viewModelScope.launch {
            repository.updateManualSpecialMode(mode)
        }
    }

    fun setRemindInterval(minutes: Int) {
        viewModelScope.launch {
            repository.updateRemindInterval(minutes)
        }
    }

    fun startService(context: Context) {
        try {
            val intent = Intent(context, EyeProtectionService::class.java).apply {
                action = EyeProtectionService.ACTION_SCREEN_ON
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun skipCurrentRest(context: Context) {
        try {
            val intent = Intent(context, EyeProtectionService::class.java).apply {
                action = EyeProtectionService.ACTION_SKIP_REST
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePause(context: Context) {
        try {
            val intent = Intent(context, EyeProtectionService::class.java).apply {
                action = EyeProtectionService.ACTION_TOGGLE_PAUSE
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetTimer(context: Context) {
        try {
            val intent = Intent(context, EyeProtectionService::class.java).apply {
                action = EyeProtectionService.ACTION_RESET_TIMER
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
