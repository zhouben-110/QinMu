package com.qinmu.eyecare.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "看板", Icons.Default.Home)
    object Statistics : Screen("statistics", "统计", Icons.Default.BarChart)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
}
