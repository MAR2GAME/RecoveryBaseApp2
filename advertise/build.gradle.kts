plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
	id("kotlin-kapt")
}

android {
	namespace = "com.pdffox.adv"
	compileSdk = 36

	defaultConfig {
		minSdk = 29

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
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
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		viewBinding = true
		buildConfig = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)

	api(platform("com.google.firebase:firebase-bom:34.2.0"))
	api("com.google.firebase:firebase-analytics")
	api("com.google.firebase:firebase-config")
	api("com.google.firebase:firebase-crashlytics-ndk")
	api("com.google.firebase:firebase-messaging")

	api("cn.thinkingdata.android:ThinkingAnalyticsSDK:3.0.3.1")
	api("com.singular.sdk:singular_sdk:12.9.1")

	api("com.squareup.okhttp3:okhttp:5.1.0")
	api("org.greenrobot:eventbus:3.3.1")

	api("com.github.bumptech.glide:glide:5.0.5")
	kapt("com.github.bumptech.glide:compiler:5.0.5")

	api("com.facebook.android:facebook-android-sdk:latest.release")

	api(libs.integrity)

	api(libs.gson)

	api("com.applovin:applovin-sdk:13.5.1")
	api("com.applovin.mediation:bigoads-adapter:+")
	api("com.applovin.mediation:chartboost-adapter:+")
	api("com.google.android.gms:play-services-base:18.9.0")
	api("com.applovin.mediation:fyber-adapter:+")
	api("com.applovin.mediation:google-ad-manager-adapter:+")
	api("com.applovin.mediation:google-adapter:+")
	api("com.applovin.mediation:inmobi-adapter:+")
	api("com.applovin.mediation:ironsource-adapter:+")
	api("com.applovin.mediation:vungle-adapter:+")
	api("com.applovin.mediation:facebook-adapter:+")
	api("com.applovin.mediation:mintegral-adapter:+")
	api("com.applovin.mediation:bytedance-adapter:+")
	api("com.applovin.mediation:unityads-adapter:+")

	api("com.google.android.ump:user-messaging-platform:4.0.0")
	api("com.google.android.gms:play-services-ads:24.8.0")
	api("com.google.android.play:app-update:2.1.0")

	api("com.google.ads.mediation:applovin:13.5.1.0")
	api("com.google.ads.mediation:chartboost:9.10.2.0")
	api("com.google.ads.mediation:fyber:8.4.1.0")
	api("com.google.ads.mediation:inmobi:11.1.0.0")
	api("com.google.ads.mediation:ironsource:9.2.0.0")
	api("com.google.ads.mediation:vungle:7.6.1.0")
	api("com.google.ads.mediation:facebook:6.21.0.0")
	api("com.google.ads.mediation:mintegral:17.0.31.0")
	api("com.google.ads.mediation:pangle:7.5.0.2.0")
	api("com.unity3d.ads:unity-ads:4.16.2")
	api("com.google.ads.mediation:unity:4.16.4.0")

	//TU (Necessary)
	api("com.thinkup.sdk:core-tpn:6.5.36.1")
	api("com.thinkup.sdk:nativead-tpn:6.5.36.1")
	api("com.thinkup.sdk:banner-tpn:6.5.36.1")
	api("com.thinkup.sdk:interstitial-tpn:6.5.36.1")
	api("com.thinkup.sdk:rewardedvideo-tpn:6.5.36.1")
	api("com.thinkup.sdk:splash-tpn:6.5.36.1")
	//Androidx (Necessary)
	api("androidx.appcompat:appcompat:1.6.1")
	api("androidx.browser:browser:1.4.0")
	//Admob
	api("com.thinkup.sdk:adapter-tpn-admob:6.5.36")
	//TU Adx SDK(Necessary)
	api("com.thinkup.sdk:adapter-tpn-sdm:6.5.36.4")
	api("com.smartdigimkttech.sdk:smartdigimkttech-sdk:6.5.40")
	//AppLovin
	api("com.thinkup.sdk:adapter-tpn-applovin:6.5.36")
	api("com.applovin:applovin-sdk:13.4.0")
	//Tramini
	api("com.thinkup.sdk:tramini-plugin-tpn:6.5.36")
	//... other dependencies
//	api("com.github.tiktok:tiktok-business-android-sdk:1.5.0")
// replace the version with the one which suits your need
//to listen for app life cycle
	api("androidx.lifecycle:lifecycle-process:2.3.1")
	api("androidx.lifecycle:lifecycle-common-java8:2.3.1")
//to get Google install referrer
	api("com.android.installreferrer:installreferrer:2.2")

	implementation(project(":safe"))
}