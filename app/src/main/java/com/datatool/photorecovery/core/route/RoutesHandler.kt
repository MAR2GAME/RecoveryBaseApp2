package com.datatool.photorecovery.core.route

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.view.CapacityScreen
import com.datatool.photorecovery.view.DebugADScreen
import com.datatool.photorecovery.view.DebugInfoScreen
import com.datatool.photorecovery.view.DebugScreen
import com.datatool.photorecovery.view.FileDetailScreen
import com.datatool.photorecovery.view.GuideScreen
import com.datatool.photorecovery.view.HomeScreen
import com.datatool.photorecovery.view.ImageDetailScreen
import com.datatool.photorecovery.view.LanguagesScreen
import com.datatool.photorecovery.view.MainScreen
import com.datatool.photorecovery.view.RecoveredFilesScreen
import com.datatool.photorecovery.view.RecoveryFileScreen
import com.datatool.photorecovery.view.RecoveryFileSuccessScreen
import com.datatool.photorecovery.view.RecoveryFolderItemScreen
import com.datatool.photorecovery.view.RecoveryFolderListScreen
import com.datatool.photorecovery.view.ScanEndScreen
import com.datatool.photorecovery.view.ScanStartScreen
import com.datatool.photorecovery.view.ScanningScreen
import com.datatool.photorecovery.view.SettingsScreen
import com.datatool.photorecovery.view.SplashScreen
import com.datatool.photorecovery.view.VideoDetailScreen
import com.datatool.photorecovery.view.WebScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun RoutesHandler() {
	val navController = LocalNavController.current
	NavHost(
		navController = navController,
		startDestination = Routes.SPLASH,
		enterTransition = {
			slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
		},
		exitTransition = {
			slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
		},
		popEnterTransition = {
			slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
		},
		popExitTransition = {
			slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
		}
	) {
		composable(Routes.SPLASH) {
			SplashScreen()
		}
		composable(Routes.GUIDE) {
			GuideScreen()
		}
		composable(Routes.HOME) {
			MainScreen()
		}
		composable(Routes.DEBUG) {
			DebugScreen()
		}
		composable(Routes.DEBUGINFO) {
			DebugInfoScreen()
		}
		composable(Routes.DEBUGAD) {
			DebugADScreen()
		}
		composable(Routes.SETTINGS) {
			SettingsScreen()
		}
		composable(
			route = "${Routes.LANGUAGES}/{isFromAppOpen}",
			arguments = listOf(
				navArgument("isFromAppOpen") { type = NavType.BoolType }
			)
		) { backStackEntry ->
			val isFromAppOpen = backStackEntry.arguments?.getBoolean("isFromAppOpen") ?: false
			LanguagesScreen(isFromAppOpen)
		}
		composable(Routes.Capacity) {
			CapacityScreen()
		}
		composable(
			route = "${Routes.Website}/{url}",
			arguments = listOf(
				navArgument("url") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val url = backStackEntry.arguments?.getString("url") ?: ""
			WebScreen(url = url)
		}
		composable(
			"${Routes.StartScan}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			ScanStartScreen(scanType = scanType)
		}
		composable(
			"${Routes.Scanning}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			ScanningScreen(scanType = scanType)
		}
		composable(
			"${Routes.ScanEnd}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			ScanEndScreen(scanType = scanType)
		}
		composable(
			"${Routes.RecoveryFolder}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			RecoveryFolderListScreen(scanType = scanType)
		}
		composable(
			"${Routes.RecoveryFolderItem}/{scanType}/{folderName}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType },
				navArgument("folderName") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			val encodedFolderName = backStackEntry.arguments?.getString("folderName") ?: ""
			val folderName = URLDecoder.decode(encodedFolderName, StandardCharsets.UTF_8.toString())
			RecoveryFolderItemScreen(scanType = scanType, foldName = folderName)
		}
		composable(
			"${Routes.FileDetail}/{filePath}/{recovered}",
			arguments = listOf(
				navArgument("filePath") { type = NavType.StringType },
				navArgument("recovered") { type = NavType.BoolType }
			)
		) { backStackEntry ->
			val encodedFilePath = backStackEntry.arguments?.getString("filePath") ?: ""
			val filePath = URLDecoder.decode(encodedFilePath, StandardCharsets.UTF_8.toString())
			val mRecovered = backStackEntry.arguments?.getBoolean("recovered") ?: false
			FileDetailScreen(filePath = filePath, recovered = mRecovered)
		}
		composable(
			"${Routes.RecoveryFile}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			RecoveryFileScreen(scanType = scanType)
		}
		composable(
			"${Routes.RecoveryFileSuccess}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			RecoveryFileSuccessScreen(scanType = scanType)
		}
		composable(
			"${Routes.RecoveredFiles}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			RecoveredFilesScreen(scanType = scanType)
		}
		composable(
			"${Routes.ViewImage}/{filePath}",
			arguments = listOf(
				navArgument("filePath") { type = NavType.StringType },
			)
		) { backStackEntry ->
			val encodedFilePath = backStackEntry.arguments?.getString("filePath") ?: ""
			val filePath = URLDecoder.decode(encodedFilePath, StandardCharsets.UTF_8.toString())
			ImageDetailScreen(filePath = filePath)
		}
		composable(
			"${Routes.ViewVideo}/{filePath}",
			arguments = listOf(
				navArgument("filePath") { type = NavType.StringType },
			)
		) { backStackEntry ->
			val encodedFilePath = backStackEntry.arguments?.getString("filePath") ?: ""
			val filePath = URLDecoder.decode(encodedFilePath, StandardCharsets.UTF_8.toString())
			VideoDetailScreen(videoPath = filePath)
		}

	}

}
