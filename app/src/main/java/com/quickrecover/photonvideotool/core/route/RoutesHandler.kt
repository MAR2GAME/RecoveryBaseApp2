package com.quickrecover.photonvideotool.core.route

import android.util.Log
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
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.core.bean.FoldBean
import com.quickrecover.photonvideotool.view.CapacityScreen
import com.quickrecover.photonvideotool.view.FileDetailScreen
import com.quickrecover.photonvideotool.view.GuideScreen
import com.quickrecover.photonvideotool.view.ImageDetailScreen
import com.quickrecover.photonvideotool.view.LanguagesScreen
import com.quickrecover.photonvideotool.view.MainScreen
import com.quickrecover.photonvideotool.view.RecoveredFilesScreen
import com.quickrecover.photonvideotool.view.RecoveryFileScreen
import com.quickrecover.photonvideotool.view.RecoveryFileSuccessScreen
import com.quickrecover.photonvideotool.view.RecoveryFolderItemScreen
import com.quickrecover.photonvideotool.view.RecoveryFolderListScreen
import com.quickrecover.photonvideotool.view.ScanEndScreen
import com.quickrecover.photonvideotool.view.ScanStartScreen
import com.quickrecover.photonvideotool.view.ScanningScreen
import com.quickrecover.photonvideotool.view.SettingsScreen
import com.quickrecover.photonvideotool.view.SplashScreen
import com.quickrecover.photonvideotool.view.VideoDetailScreen
import com.quickrecover.photonvideotool.view.WebScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// TODO: add
//import com.quickrecover.photonvideotool.view.DebugScreen

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
		// TODO: add
//		composable(Routes.DEBUG) {
//			DebugScreen()
//		}
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
			"${Routes.RecoveryFolderItem}/{scanType}",
			arguments = listOf(
				navArgument("scanType") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val scanType = backStackEntry.arguments?.getString("scanType") ?: "recovery_photos"
			val foldBean = navController.previousBackStackEntry?.savedStateHandle?.get<FoldBean>("foldBean")
			Log.e("TAG", "RoutesHandler: " + "${Routes.RecoveryFolderItem}/{scanType}" + " , ${foldBean?.files?.size}" )
			RecoveryFolderItemScreen(scanType = scanType, foldBean = foldBean ?: FoldBean())
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