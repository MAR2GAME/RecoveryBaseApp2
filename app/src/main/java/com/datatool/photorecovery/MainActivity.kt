package com.datatool.photorecovery

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.core.route.RoutesHandler
import com.datatool.photorecovery.ui.theme.PhotoRecoveryTheme
import com.pdffox.adv.AdvertiseSdk

val LocalNavController = staticCompositionLocalOf<NavHostController> {
	error("No NavHostController provided")
}

val LocalInnerPadding = staticCompositionLocalOf<PaddingValues> {
	error("No inner padding provided")
}

class MainActivity : ComponentActivity() {

	companion object {
		private const val TAG = "MainActivity"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.e(TAG, "onCreate: ")
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		enableEdgeToEdge()
		getFromParams()
		AdvertiseSdk.setUserOnceAttr(AdvertiseSdk.ThinkingKeys.firstOpenTime, AdvertiseSdk.getFirstOpenTime())
		AdvertiseSdk.setUserAttr(AdvertiseSdk.ThinkingKeys.latestOpenTime, AdvertiseSdk.getLatestOpenTime())

		setContent {
			PhotoRecoveryTheme {
				Scaffold(
					modifier = Modifier.fillMaxSize(),
				) { innerPadding ->
					val navController = rememberNavController()
					CompositionLocalProvider(
						LocalNavController provides navController,
						LocalInnerPadding provides innerPadding,
					) {
						RoutesHandler()
					}
				}
			}
		}
	}

	override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
		super.onNewIntent(intent, caller)
		setIntent(intent)
		getFromParams()
	}

	override fun onResume() {
		super.onResume()
		val appOpenFrom = intent.getStringExtra("AppOpenFrom")
		if (appOpenFrom == "Push") {
			val paramsNoticeId = intent.getStringExtra("NoticeId") ?: ""
			val paramsDistinctId = intent.getStringExtra("DistinctId") ?: ""
			Log.e(TAG, "onResume: from $appOpenFrom, noticeId = $paramsNoticeId, distinctId = $paramsDistinctId")
			AdvertiseSdk.logEvent(
				AdvertiseSdk.PushLog.notificationClicked,
				mapOf(
					AdvertiseSdk.PushLog.msgId to paramsNoticeId,
					AdvertiseSdk.PushLog.targetUserId to paramsDistinctId,
				),
			)
		}
		if (appOpenFrom == "persistent") {
			Log.e(TAG, "onResume: opened from persistent notification")
		}
		AdvertiseSdk.setSuperProperties(mapOf("traffic_source" to if (appOpenFrom == "Push") "fcm_push" else appOpenFrom))
		AdvertiseSdk.ensurePersistentNotificationServiceRunning(this)
	}

	var route: String = ""

	fun getFromParams() {
		val appOpenFrom = intent.getStringExtra("AppOpenFrom") ?: ""
		Log.e(TAG, "getAppOpenFrom: $appOpenFrom")

		if (appOpenFrom == "persistent") {
			Log.e(TAG, "getFromParams: opened from persistent notification")
		}
		AdvertiseSdk.setSuperProperties(mapOf("traffic_source" to if (appOpenFrom == "Push") "fcm_push" else appOpenFrom))

		route = intent.getStringExtra("Route") ?: ""
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "getFromParams: route = $route")
		}
		val scene = intent.getStringExtra("Scene") ?: ""
		Log.e(TAG, "getFromParams: $scene")
		if (scene.isNotEmpty()) {
			val logParams = mutableMapOf<String, Any>()
			logParams["scene"] = scene
			AdvertiseSdk.logEvent("notification_app_clicked", logParams)
			Log.e(TAG, "notification_app_clicked: $logParams")
		}
	}
}
