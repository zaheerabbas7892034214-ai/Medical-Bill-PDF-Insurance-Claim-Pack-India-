package com.medicalbill.claimpack.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medicalbill.claimpack.ui.screens.*

@Composable
fun MedicalBillApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val isProUnlocked by viewModel.isProUnlocked.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                isProUnlocked = isProUnlocked,
                onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToConvert = { fileUri ->
                    navController.navigate(Screen.ConvertProgress.createRoute(fileUri))
                }
            )
        }
        
        composable(
            route = Screen.ConvertProgress.route,
            arguments = listOf(navArgument("fileUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileUri = backStackEntry.arguments?.getString("fileUri") ?: ""
            ConvertProgressScreen(
                fileUri = fileUri,
                onConversionComplete = { fileId ->
                    navController.navigate(Screen.Preview.createRoute(fileId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Preview.route,
            arguments = listOf(navArgument("fileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
            PreviewScreen(
                fileId = fileId,
                isProUnlocked = isProUnlocked,
                onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) },
                onNavigateToClaimPack = { navController.navigate(Screen.ClaimPackPreview.createRoute(fileId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ClaimPackPreview.route,
            arguments = listOf(navArgument("fileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
            ClaimPackPreviewScreen(
                fileId = fileId,
                isProUnlocked = isProUnlocked,
                onNavigateToExport = { navController.navigate(Screen.Export.createRoute(fileId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Export.route,
            arguments = listOf(navArgument("fileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
            ExportScreen(
                fileId = fileId,
                isProUnlocked = isProUnlocked,
                onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Paywall.route) {
            PaywallScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
