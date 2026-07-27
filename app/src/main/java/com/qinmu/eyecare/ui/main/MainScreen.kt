package com.qinmu.eyecare.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qinmu.eyecare.ui.screens.dashboard.DashboardScreen
import com.qinmu.eyecare.ui.screens.settings.SettingsScreen
import com.qinmu.eyecare.ui.screens.statistics.StatisticsScreen
import com.qinmu.eyecare.ui.theme.*

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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            SkyGradientStart,
            SkyGradientEnd,
            SkyBackgroundLight
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // Figma Inspired Floating Neumorphic Pill Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .neumorphicShadow(
                                cornerRadius = 34.dp,
                                elevation = 8.dp
                            )
                            .clip(RoundedCornerShape(34.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        NeumorphicCardSurface,
                                        NeumorphicSurface
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(34.dp)
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            screens.forEach { screen ->
                                val selected = currentRoute == screen.route

                                val iconColor by animateColorAsState(
                                    targetValue = if (selected) AccentRoyalBlue else TextSecondaryBlue,
                                    label = "iconColor"
                                )

                                val activePillBg by animateColorAsState(
                                    targetValue = if (selected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                                    label = "pillBg"
                                )

                                val interactionSource = remember { MutableInteractionSource() }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(activePillBg)
                                        .then(
                                            if (selected) {
                                                Modifier.border(
                                                    1.dp,
                                                    Color.White,
                                                    RoundedCornerShape(24.dp)
                                                )
                                            } else Modifier
                                        )
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        if (selected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = screen.title,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentRoyalBlue,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val routeOrder = listOf(Screen.Dashboard.route, Screen.Statistics.route, Screen.Settings.route)

            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    val initialIndex = routeOrder.indexOf(initialState.destination.route).coerceAtLeast(0)
                    val targetIndex = routeOrder.indexOf(targetState.destination.route).coerceAtLeast(0)
                    val isForward = targetIndex > initialIndex
                    val slideOffset = if (isForward) 1f else -1f

                    slideInHorizontally(
                        initialOffsetX = { (it * slideOffset).toInt() },
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 280)
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    val initialIndex = routeOrder.indexOf(initialState.destination.route).coerceAtLeast(0)
                    val targetIndex = routeOrder.indexOf(targetState.destination.route).coerceAtLeast(0)
                    val isForward = targetIndex > initialIndex
                    val slideOffset = if (isForward) -0.3f else 0.3f

                    slideOutHorizontally(
                        targetOffsetX = { (it * slideOffset).toInt() },
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 240)
                    ) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                },
                popEnterTransition = {
                    val initialIndex = routeOrder.indexOf(initialState.destination.route).coerceAtLeast(0)
                    val targetIndex = routeOrder.indexOf(targetState.destination.route).coerceAtLeast(0)
                    val isForward = targetIndex > initialIndex
                    val slideOffset = if (isForward) 1f else -1f

                    slideInHorizontally(
                        initialOffsetX = { (it * slideOffset).toInt() },
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 280)
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                },
                popExitTransition = {
                    val initialIndex = routeOrder.indexOf(initialState.destination.route).coerceAtLeast(0)
                    val targetIndex = routeOrder.indexOf(targetState.destination.route).coerceAtLeast(0)
                    val isForward = targetIndex > initialIndex
                    val slideOffset = if (isForward) -0.3f else 0.3f

                    slideOutHorizontally(
                        targetOffsetX = { (it * slideOffset).toInt() },
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 240)
                    ) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                }
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
}
