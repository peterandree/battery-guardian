package com.batteryguardian

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.batteryguardian.ui.screen.DeviceDetailScreen
import com.batteryguardian.ui.screen.MainScreen
import com.batteryguardian.ui.screen.SettingsScreen

/**
 * Navigation host for Battery Guardian.
 * 
 * Defines all navigation destinations and handles navigation between screens.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        // Main screen - list of all devices
        composable("main") {
            MainScreen(
                onDeviceClick = { deviceId ->
                    navController.navigate("device/$deviceId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        
        // Device detail screen
        composable("device/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId")
                ?: return@composable
            DeviceDetailScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        
        // Settings screen
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
