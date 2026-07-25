package com.qinmu.eyecare.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qinmu.eyecare.ui.screens.dashboard.DashboardScreen
import com.qinmu.eyecare.ui.screens.settings.SettingsScreen
import com.qinmu.eyecare.ui.screens.statistics.StatisticsScreen
import com.qinmu.eyecare.ui.theme.SpotifyGreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Dashboard,
        Screen.Statistics,
        Screen.Settings
    )

    Scaffold(
        containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkBase,
        bottomBar = {
            NavigationBar(
                containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                            selectedTextColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                            unselectedIconColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                            unselectedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                            indicatorColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
