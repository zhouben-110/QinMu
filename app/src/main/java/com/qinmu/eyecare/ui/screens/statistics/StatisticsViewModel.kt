package com.qinmu.eyecare.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qinmu.eyecare.QinMuApplication
import com.qinmu.eyecare.data.model.UsageLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = (application as QinMuApplication).database.usageLogDao()

    val recent7DaysLogs: StateFlow<List<UsageLogEntity>> = dao.getRecent7DaysLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
