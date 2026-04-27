package com.quickrecover.photonvideotool

import android.app.ComponentCaller
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.quickrecover.photonvideotool.core.route.RoutesHandler
import com.quickrecover.photonvideotool.ui.theme.PhotoRecoveryTheme

// TODO: add
//import cn.thinkingdata.analytics.TDAnalytics
//import com.pdffox.adv.use.adv.AdvActivity
//import com.pdffox.adv.use.log.LogUtil
//import com.pdffox.adv.use.log.ThinkingAttr
//import com.pdffox.adv.use.push.LogPushData
//import com.pdffox.adv.use.push.LogPushParam
//import com.quickrecover.photonvideotool.notification.CommonService

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.util.Locale
import kotlin.collections.get
import kotlin.getValue

val LocalNavController = staticCompositionLocalOf<NavHostController> {
	error("No NavHostController provided")
}

val LocalInnerPadding = staticCompositionLocalOf<PaddingValues> {
	error("No inner padding provided")
}

// TODO:
//class MainActivity : AdvActivity() {
class MainActivity : AppCompatActivity() {

	companion object {
		private const val TAG = "MainActivity"
		var cachedLanguage: String? = null
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.e(TAG, "onCreate: ")
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		enableEdgeToEdge()
		getLanguage()
		getFromParams()

		// TODO:
//		ThinkingAttr.setUserOnceAttr(ThinkingAttr.first_visit_country, ThinkingAttr.getFirstCountry(this))
//		ThinkingAttr.setUserOnceAttr(ThinkingAttr.first_visit_language, ThinkingAttr.getFirstLanguage(this))
//		ThinkingAttr.setUserOnceAttr(ThinkingAttr.first_open_time, ThinkingAttr.getFirstOpenTime())
//		ThinkingAttr.setUserAttr(ThinkingAttr.latest_open_time, ThinkingAttr.getLatestOpenTime())
//		ThinkingAttr.setUserAddAttr(ThinkingAttr.total_open_num, 1)

//		val logger = AppEventsLogger.newLogger(this)
//		logger.logEvent("battledAnOrcAAA")
//		Log.e(TAG, "onCreate: facebook打点 battledAnOrcAAA", )
		setContent {
			PhotoRecoveryTheme {
				Scaffold(
					modifier = Modifier.fillMaxSize(),
				) { innerPadding ->
					val navController = rememberNavController()
					CompositionLocalProvider(
						LocalNavController provides navController,
						LocalInnerPadding provides innerPadding
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
		//"如果用户是从推送启动应用，则在启动时将属性值设为fcm_push,
		//如果通过静默通知启动应用，则设为persistent，
		//如果是通过app发的场景化推送点击，则设为app_push,
		val appOpenFrom = intent.getStringExtra("AppOpenFrom")
		// TODO:
//		if (appOpenFrom == "Push"){
//			val paramsNoticeId = intent.getStringExtra("NoticeId") ?: ""
//			val paramsDistinctId = intent.getStringExtra("DistinctId") ?: ""
//			Log.e(TAG, "onResume: from $appOpenFrom, noticeId = $paramsNoticeId, distinctId = $paramsDistinctId")
//			LogUtil.log(LogPushData.notification_clicked,mapOf(
//				LogPushParam.msg_id to paramsNoticeId,
//				LogPushParam.target_user_id to paramsDistinctId,
//			))
//		}
//		if (appOpenFrom == "persistent"){
//			Log.e(TAG, "onResume: 从静态通知栏打开")
//		}
//		val superProperties = JSONObject().apply {
//			put("traffic_source", if (appOpenFrom == "Push") "fcm_push" else appOpenFrom)
//		}
//		TDAnalytics.setSuperProperties(superProperties)
//		startForegroundService(Intent(this, CommonService::class.java))
	}

	var route: String = ""
	fun getFromParams() {
		val appOpenFrom = intent.getStringExtra("AppOpenFrom") ?: ""
		Log.e(TAG, "getAppOpenFrom: $appOpenFrom" )

		if (appOpenFrom == "persistent"){
			Log.e(TAG, "onResume: 从静态通知栏打开")
		}

		// TODO:
//		val superProperties = JSONObject().apply {
//			put("traffic_source", if (appOpenFrom == "Push") "fcm_push" else appOpenFrom)
//		}
//		TDAnalytics.setSuperProperties(superProperties)

		route = intent.getStringExtra("Route") ?: ""
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "getFromParams: route = $route", )
		}
		// TODO:
//		val scene = intent.getStringExtra("Scene") ?: ""
//		Log.e(TAG, "getFromParams: $scene", )
//		if (scene.isNotEmpty()){
//			val logParams = mutableMapOf<String, Any>()
//			logParams["scene"] = scene
//			LogUtil.log("notification_app_clicked",logParams)
//			Log.e(TAG, "notification_app_clicked: $logParams", )
//		}
	}

	fun getLanguage() {
		val locale = runBlocking {
			getCurrentLocale()
		}
		cachedLanguage = locale.language
	}

	private val dataStore: DataStore<Preferences> by inject()

	private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

	val LANGUAGE_MAP: Map<String, Locale> = mapOf(
		"العربية" to Locale("ar"),
		"čeština" to Locale("cs"),
		"dansk" to Locale("da"),
		"Deutsch" to Locale("de"),
		"Eλληνικά" to Locale("el"),
		"English" to Locale.ENGLISH,
		"español" to Locale("es"),
		"svenska" to Locale("sv"),
		"français" to Locale.FRENCH,
		"Indonesia" to Locale("id"),
		"italiano" to Locale.ITALIAN,
		"日本語" to Locale.JAPANESE,
		"한국어" to Locale.KOREAN,
		"Nederlands" to Locale("nl"),
		"norsk" to Locale("no"),
		"polski" to Locale("pl"),
		"português" to Locale("pt"),
		"română" to Locale("ro"),
		"Türkçe" to Locale("tr"),
		"中文 (简体)" to Locale("zh", "CN"),
		"中文 (繁体)" to Locale("zh", "TW")
	)
	suspend fun getCurrentLocale(): Locale {
		val displayName = dataStore
			.data
			.map { preferences ->
				preferences[LANGUAGE_KEY]
			}
			.first()
		return LANGUAGE_MAP[displayName] ?: Locale.getDefault()
	}

	override fun attachBaseContext(baseContext: Context) {
		getLanguage()
		Log.e(TAG, "attachBaseContext: $cachedLanguage", )
		val language = cachedLanguage ?: ""
		val context = newWrap(baseContext, language)
		super.attachBaseContext(context)
	}

	/**
	 *  创建ContextWrapper对象，
	 */
	private fun newWrap(context: Context, language: String): ContextWrapper {
		val configuration = context.resources.configuration
		configuration.fontScale = 1f
		val locale = Locale(language)
		val localeList = LocaleList(locale)
		LocaleList.setDefault(localeList)
		configuration.setLocales(localeList)
		return ContextWrapper(context.createConfigurationContext(configuration))
	}

}
