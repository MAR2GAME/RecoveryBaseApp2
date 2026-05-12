import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	id("kotlin-parcelize")
	id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")
	id("kotlin-kapt")
}

val localProperties = Properties().apply {
	val file = rootProject.file("local.properties")
	if (file.isFile) {
		file.inputStream().use(::load)
	}
}

// advertise 参数读取优先级：Gradle property -> local.properties -> 环境变量 -> 默认值。
fun advStringProperty(propertyName: String, envName: String, defaultValue: String = ""): String {
	return providers.gradleProperty(propertyName).orNull
		?: localProperties.getProperty(propertyName)
		?: providers.environmentVariable(envName).orNull
		?: defaultValue
}

fun advBooleanProperty(propertyName: String, envName: String, defaultValue: Boolean = false): Boolean {
	return advStringProperty(propertyName, envName, defaultValue.toString())
		.toBooleanStrictOrNull()
		?: defaultValue
}

fun advEnabledProperty(propertyName: String, envName: String): Boolean {
	return advBooleanProperty(propertyName, envName, true)
}

fun advLongProperty(propertyName: String, envName: String, defaultValue: Long = 0L): Long {
	return advStringProperty(propertyName, envName, defaultValue.toString())
		.toLongOrNull()
		?: defaultValue
}

fun String.asBuildConfigString(): String {
	return "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

android {
	// 当前 app 模块的资源和代码命名空间。
	namespace = "com.datatool.photorecovery"
	// 编译使用的 Android SDK 版本。
	compileSdk = 36

	defaultConfig {
		// 应用包名，必须与 Firebase、广告后台和安全校验配置保持一致。
		applicationId = "com.datatool.photorecovery"
		// 支持安装的最低 Android SDK 版本。
		minSdk = 29
		// 目标 Android SDK 版本。
		targetSdk = 36
		// 应用内部版本号，上架发版时需要递增。
		versionCode = 53
		// 应用展示版本号。
		versionName = "1.5.3"

		// Android Instrumentation 测试运行器。
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

		fun advStringField(name: String, value: String) =
			buildConfigField("String", name, value.asBuildConfigString())

		fun advStringField(
			name: String,
			propertyName: String,
			envName: String,
			defaultValue: String = "",
		) =
			advStringField(name, advStringProperty(propertyName, envName, defaultValue))

		fun advBooleanField(name: String, value: Boolean) =
			buildConfigField("boolean", name, value.toString())

		fun advBooleanField(
			name: String,
			propertyName: String,
			envName: String,
			defaultValue: Boolean = false,
		) =
			advBooleanField(name, advBooleanProperty(propertyName, envName, defaultValue))

		fun advEnabledField(name: String, propertyName: String, envName: String) =
			advBooleanField(name, advEnabledProperty(propertyName, envName))

		fun advLongField(name: String, propertyName: String, envName: String, defaultValue: Long = 0L) =
			buildConfigField("long", name, "${advLongProperty(propertyName, envName, defaultValue)}L")

		fun advStringPlaceholder(
			name: String,
			propertyName: String,
			envName: String,
			defaultValue: String = "",
		) {
			manifestPlaceholders[name] = advStringProperty(propertyName, envName, defaultValue)
		}

		fun advEnabledPlaceholder(name: String, propertyName: String, envName: String) {
			manifestPlaceholders[name] = advEnabledProperty(propertyName, envName).toString()
		}

		// 输出 APK/AAB 文件名使用包名和版本名。
		setProperty("archivesBaseName", "${applicationId}_${versionName}")

		// 隐私政策页面 URL，用于设置页、合规入口和广告 SDK 合规展示。
		advStringField("ADV_PRIVACY_URL", "adv.privacyUrl", "ADV_PRIVACY_URL", "https://sites.google.com/view/photo-recovery-privacy-policy/")
		// 用户协议页面 URL，用于设置页和合规入口。
		advStringField("ADV_TERMS_URL", "adv.termsUrl", "ADV_TERMS_URL", "https://sites.google.com/view/photo-recovery-terms-condition/")

		// 是否启用 advertise 后端接口能力；关闭后广告限频、配置拉取和 token 解析会跳过服务端。
		advEnabledField("ADV_SERVER_ENABLED", "adv.server.enabled", "ADV_SERVER_ENABLED")
		// 生产环境 advertise 后端域名。
		advStringField("ADV_SERVER_RELEASE_HOST", "adv.server.releaseHost", "ADV_SERVER_RELEASE_HOST", "https://api.newminigame.online")
		// 测试环境 advertise 后端域名，仅 Debug/isTest 场景使用。
		advStringField("ADV_SERVER_TEST_HOST", "adv.server.testHost", "ADV_SERVER_TEST_HOST", "http://192.168.110.68:10002")
		// Play Integrity token 解析接口的服务端密钥。
		advStringField("ADV_SERVER_PARSE_TOKEN_KEY", "adv.server.parseTokenKey", "ADV_SERVER_PARSE_TOKEN_KEY", "TianWangGaiDiHu")

		// 是否启用 Firebase Analytics 事件上报。
		advEnabledField("ADV_FIREBASE_ANALYTICS_ENABLED", "adv.firebase.analyticsEnabled", "ADV_FIREBASE_ANALYTICS_ENABLED")
		// 是否启用 Firebase Messaging 推送接收。
		advEnabledField("ADV_FIREBASE_MESSAGING_ENABLED", "adv.firebase.messagingEnabled", "ADV_FIREBASE_MESSAGING_ENABLED")
		// 是否启动后订阅默认 FCM topic。
		advEnabledField("ADV_FIREBASE_SUBSCRIBE_DEFAULT_TOPIC", "adv.firebase.subscribeDefaultTopic", "ADV_FIREBASE_SUBSCRIBE_DEFAULT_TOPIC")
		// 是否启用 Firebase Remote Config 拉取广告策略和推送配置。
		advEnabledField("ADV_REMOTE_CONFIG_ENABLED", "adv.remoteConfig.enabled", "ADV_REMOTE_CONFIG_ENABLED")

		// 是否启用 ThinkingData 统计。
		advEnabledField("ADV_THINKING_ENABLED", "adv.thinking.enabled", "ADV_THINKING_ENABLED")
		// ThinkingData 项目 App Key。
		advStringField("ADV_THINKING_APP_KEY", "adv.thinking.appKey", "ADV_THINKING_APP_KEY", "1467641c0ffa4ef6a20f8a8b7e1d64a7")
		// ThinkingData 服务端地址。
		advStringField("ADV_THINKING_SERVER_URL", "adv.thinking.serverUrl", "ADV_THINKING_SERVER_URL", "https://mar2.top")

		// 是否启用 Singular 归因和广告收益上报。
		advEnabledField("ADV_SINGULAR_ENABLED", "adv.singular.enabled", "ADV_SINGULAR_ENABLED")
		// Singular SDK API Key。
		advStringField("ADV_SINGULAR_API_KEY", "adv.singular.apiKey", "ADV_SINGULAR_API_KEY", "mar2game_f7b9272a")
		// Singular SDK Secret。
		advStringField("ADV_SINGULAR_SECRET", "adv.singular.secret", "ADV_SINGULAR_SECRET", "72b3df2ee5d0a64a6c404ce01937c3d6")

		// AdMob App ID，写入 AndroidManifest 供 Google Mobile Ads 初始化读取。
		advStringPlaceholder("advAdmobAppId", "adv.admob.appId", "ADV_ADMOB_APP_ID", "ca-app-pub-3615322193850391~3893272881")
		// AdMob App ID，写入 BuildConfig 供 advertise 初始化配置使用。
		advStringField("ADV_ADMOB_APP_ID", "adv.admob.appId", "ADV_ADMOB_APP_ID", "ca-app-pub-3615322193850391~3893272881")
		// AdMob Banner 广告位 ID。
		advStringField("ADV_ADMOB_BANNER_ID", "adv.admob.bannerId", "ADV_ADMOB_BANNER_ID", "ca-app-pub-3615322193850391/4485010266")
		// AdMob 插屏广告位 ID。
		advStringField("ADV_ADMOB_INTERSTITIAL_ID", "adv.admob.interstitialId", "ADV_ADMOB_INTERSTITIAL_ID", "ca-app-pub-3615322193850391/2830923916")
		// AdMob 原生广告位 ID。
		advStringField("ADV_ADMOB_NATIVE_ID", "adv.admob.nativeId", "ADV_ADMOB_NATIVE_ID", "ca-app-pub-3615322193850391/5283086610")
		// AdMob 开屏广告位 ID。
		advStringField("ADV_ADMOB_OPEN_ID", "adv.admob.openId", "ADV_ADMOB_OPEN_ID", "ca-app-pub-3615322193850391/9204760570")

		// 是否启用 Facebook SDK 初始化和事件能力。
		advEnabledField("ADV_FACEBOOK_ENABLED", "adv.facebook.enabled", "ADV_FACEBOOK_ENABLED")
		// Facebook App ID，写入 AndroidManifest 供 Facebook SDK 读取。
		advStringPlaceholder("advFacebookAppId", "adv.facebook.appId", "ADV_FACEBOOK_APP_ID", "1590185508637811")
		// Facebook Client Token，写入 AndroidManifest 供 Facebook SDK 读取。
		advStringPlaceholder("advFacebookClientToken", "adv.facebook.clientToken", "ADV_FACEBOOK_CLIENT_TOKEN", "6d8edd1c9853e57c091f57e390421ddd")
		// Facebook App ID，写入 BuildConfig 供 advertise 初始化配置使用。
		advStringField("ADV_FACEBOOK_APP_ID", "adv.facebook.appId", "ADV_FACEBOOK_APP_ID", "1590185508637811")
		// Facebook Client Token，写入 BuildConfig 供 advertise 初始化配置使用。
		advStringField("ADV_FACEBOOK_CLIENT_TOKEN", "adv.facebook.clientToken", "ADV_FACEBOOK_CLIENT_TOKEN", "6d8edd1c9853e57c091f57e390421ddd")

		// 是否启用 TikTok Business SDK。
		advEnabledField("ADV_TIKTOK_ENABLED", "adv.tiktok.enabled", "ADV_TIKTOK_ENABLED")
		// TikTok Business SDK access token。
		advStringField("ADV_TIKTOK_ACCESS_TOKEN", "adv.tiktok.accessToken", "ADV_TIKTOK_ACCESS_TOKEN", "TTpjhQJCkNhW2m9kobQVUiIOUciGopjh")
		// TikTok Business SDK 绑定的 Android app id，默认使用包名。
		advStringField("ADV_TIKTOK_APP_ID", "adv.tiktok.appId", "ADV_TIKTOK_APP_ID", "com.datatool.photorecovery")
		// TikTok 广告后台分配的 TT App ID。
		advStringField("ADV_TIKTOK_TT_APP_ID", "adv.tiktok.ttAppId", "ADV_TIKTOK_TT_APP_ID", "7624342473057271816")

		// 是否启用包名、签名、调试状态等安全校验。
		advEnabledField("ADV_SAFE_ENABLED", "adv.safe.enabled", "ADV_SAFE_ENABLED")
		// 允许通过安全校验的发布签名 SHA-256 列表，多个值用逗号分隔。
		advStringField(
			"ADV_SAFE_EXPECTED_SIGNATURES",
			"adv.safe.expectedSignatures",
			"ADV_SAFE_EXPECTED_SIGNATURES",
			"BC9226C0D24125D7BFF05CF3D746EFFCF72AB101E8B14BAFB1EB7C08557BECDC,4CCC0599E4F4718BEA0E7BE46A21D4FC0D5F35656CAD670784AE5893DB0C075D",
		)
		// 是否拒绝 debuggable 构建通过安全校验。
		advBooleanField("ADV_SAFE_REJECT_DEBUGGABLE_BUILDS", "adv.safe.rejectDebuggableBuilds", "ADV_SAFE_REJECT_DEBUGGABLE_BUILDS", true)
		// 是否拒绝正在被调试器附加的进程。
		advBooleanField("ADV_SAFE_REJECT_DEBUGGER_ATTACHED", "adv.safe.rejectDebuggerAttached", "ADV_SAFE_REJECT_DEBUGGER_ATTACHED", true)
		// 安全校验失败时是否直接结束进程。
		advBooleanField("ADV_SAFE_KILL_PROCESS_ON_FAILURE", "adv.safe.killProcessOnFailure", "ADV_SAFE_KILL_PROCESS_ON_FAILURE", true)

		// 是否启用 advertise 推送相关能力总开关。
		advEnabledField("ADV_PUSH_ENABLED", "adv.push.enabled", "ADV_PUSH_ENABLED")
		// 是否在 Manifest 启用常驻通知服务组件。
		advEnabledPlaceholder("advPersistentServiceEnabled", "adv.push.persistentServiceEnabled", "ADV_PUSH_PERSISTENT_SERVICE_ENABLED")
		// 是否在 Manifest 启用 Firebase Messaging Service 组件。
		advEnabledPlaceholder("advFirebaseMessagingServiceEnabled", "adv.push.firebaseMessagingServiceEnabled", "ADV_PUSH_FIREBASE_MESSAGING_SERVICE_ENABLED")
		// 是否在 Manifest 启用 JobService 启动器组件。
		advEnabledPlaceholder("advServiceStarterJobEnabled", "adv.push.serviceStarterJobEnabled", "ADV_PUSH_SERVICE_STARTER_JOB_ENABLED")
		// 是否在 Manifest 启用通知删除广播接收器。
		advEnabledPlaceholder("advNotificationDeletedReceiverEnabled", "adv.push.notificationDeletedReceiverEnabled", "ADV_PUSH_NOTIFICATION_DELETED_RECEIVER_ENABLED")
		// 是否在 Manifest 启用开机广播接收器。
		advEnabledPlaceholder("advBootReceiverEnabled", "adv.push.bootReceiverEnabled", "ADV_PUSH_BOOT_RECEIVER_ENABLED")
		// 是否在 Manifest 启用推送附件 FileProvider。
		advEnabledPlaceholder("advFileProviderEnabled", "adv.push.fileProviderEnabled", "ADV_PUSH_FILE_PROVIDER_ENABLED")
		// 是否在运行时启用常驻通知服务逻辑。
		advEnabledField("ADV_PUSH_PERSISTENT_SERVICE_ENABLED", "adv.push.persistentServiceEnabled", "ADV_PUSH_PERSISTENT_SERVICE_ENABLED")
		// 是否在运行时启用 Firebase Messaging Service 逻辑。
		advEnabledField("ADV_PUSH_FIREBASE_MESSAGING_SERVICE_ENABLED", "adv.push.firebaseMessagingServiceEnabled", "ADV_PUSH_FIREBASE_MESSAGING_SERVICE_ENABLED")
		// 是否在运行时启用 JobService 启动器逻辑。
		advEnabledField("ADV_PUSH_SERVICE_STARTER_JOB_ENABLED", "adv.push.serviceStarterJobEnabled", "ADV_PUSH_SERVICE_STARTER_JOB_ENABLED")
		// 是否在运行时启用开机广播逻辑。
		advEnabledField("ADV_PUSH_BOOT_RECEIVER_ENABLED", "adv.push.bootReceiverEnabled", "ADV_PUSH_BOOT_RECEIVER_ENABLED")
		// 是否在运行时启用通知删除广播逻辑。
		advEnabledField("ADV_PUSH_NOTIFICATION_DELETED_RECEIVER_ENABLED", "adv.push.notificationDeletedReceiverEnabled", "ADV_PUSH_NOTIFICATION_DELETED_RECEIVER_ENABLED")
		// 是否在运行时启用推送附件 FileProvider。
		advEnabledField("ADV_PUSH_FILE_PROVIDER_ENABLED", "adv.push.fileProviderEnabled", "ADV_PUSH_FILE_PROVIDER_ENABLED")
		// 是否启用图片/视频/文件删除监听触发推送场景。
		advEnabledField("ADV_PUSH_DELETION_OBSERVER_ENABLED", "adv.push.deletionObserverEnabled", "ADV_PUSH_DELETION_OBSERVER_ENABLED")
		// 充电开始时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_CHARGING_STARTED", "adv.push.scene.chargingStarted", "ADV_PUSH_SCENE_CHARGING_STARTED", "charging_start")
		// 充电结束时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_CHARGING_ENDED", "adv.push.scene.chargingEnded", "ADV_PUSH_SCENE_CHARGING_ENDED", "charging_end")
		// 亮屏时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_SCREEN_ON", "adv.push.scene.screenOn", "ADV_PUSH_SCENE_SCREEN_ON", "screen_on_5s")
		// 用户解锁时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_USER_PRESENT", "adv.push.scene.userPresent", "ADV_PUSH_SCENE_USER_PRESENT", "phone_unlock")
		// 安装应用时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_PACKAGE_ADDED", "adv.push.scene.packageAdded", "ADV_PUSH_SCENE_PACKAGE_ADDED", "app_installed")
		// 卸载应用时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_PACKAGE_REMOVED", "adv.push.scene.packageRemoved", "ADV_PUSH_SCENE_PACKAGE_REMOVED", "app_uninstalled")
		// 删除图片时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_IMAGE_DELETED", "adv.push.scene.imageDeleted", "ADV_PUSH_SCENE_IMAGE_DELETED", "delete_photos")
		// 删除视频时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_VIDEO_DELETED", "adv.push.scene.videoDeleted", "ADV_PUSH_SCENE_VIDEO_DELETED", "delete_videos")
		// 删除其他文件时使用的推送场景 key。
		advStringField("ADV_PUSH_SCENE_FILE_DELETED", "adv.push.scene.fileDeleted", "ADV_PUSH_SCENE_FILE_DELETED", "delete_files")

		// 是否启用通知展示和通知路由能力。
		advEnabledField("ADV_NOTIFICATIONS_ENABLED", "adv.notifications.enabled", "ADV_NOTIFICATIONS_ENABLED")
		// 是否启用 Play Integrity 校验。
		advEnabledField("ADV_PLAY_INTEGRITY_ENABLED", "adv.playIntegrity.enabled", "ADV_PLAY_INTEGRITY_ENABLED")
		// Play Integrity 绑定的 Google Cloud project number。
		advLongField("ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "adv.playIntegrity.cloudProjectNumber", "ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", 804850522653L)
	}

	buildTypes {
		release {
			// Release 默认订阅正式 FCM topic。
			buildConfigField("String", "ADV_DEFAULT_TOPIC", advStringProperty("adv.defaultTopic", "ADV_DEFAULT_TOPIC", "all").asBuildConfigString())
			// Release 是否启用 AdMob 广告加载。
			buildConfigField("boolean", "ADV_ADMOB_ENABLED", advEnabledProperty("adv.admob.enabled", "ADV_ADMOB_ENABLED").toString())
			// Release 开启代码混淆。
			isMinifyEnabled = true
			// Release 开启资源压缩。
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
		debug {
			// Debug 默认订阅调试 FCM topic，避免污染正式 topic。
			buildConfigField("String", "ADV_DEFAULT_TOPIC", advStringProperty("adv.defaultTopic", "ADV_DEFAULT_TOPIC", "debug-all").asBuildConfigString())
			// Debug 是否启用 AdMob 广告加载。
			buildConfigField("boolean", "ADV_ADMOB_ENABLED", advEnabledProperty("adv.admob.enabled", "ADV_ADMOB_ENABLED").toString())
			// Debug 保持未混淆，方便调试。
			isMinifyEnabled = false
			// Debug 不压缩资源，方便排查资源问题。
			isShrinkResources = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		// 启用 Jetpack Compose。
		compose = true
		// 生成 BuildConfig，advertise 宿主参数依赖这里的字段。
		buildConfig = true
	}
}

kotlin {
	compilerOptions {
		// Kotlin 编译目标 JVM 版本。
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.navigation.runtime.ktx)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.datastore.preferences.core)
	implementation(libs.androidx.constraintlayout.compose)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.lifecycle.process)

	implementation(libs.kotlinx.serialization.json)

	implementation(libs.lottie.compose)

	implementation(libs.koin.core)
	implementation(libs.koin.android)
	implementation(libs.koin.androidx.compose)

	implementation(libs.coil.compose)
	implementation(libs.glide)
	implementation(libs.gson)

	kapt(libs.compiler)

	implementation(libs.androidx.media3.exoplayer)
	implementation(libs.androidx.media3.ui)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)

//	implementation(project(":advertise"))

}
