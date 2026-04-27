buildscript {
	repositories {
		google()
		mavenCentral()
		maven(url = "https://artifacts.applovin.com/android")
		maven(url = "https://maven.singular.net/")
		maven(url = "https://cboost.jfrog.io/artifactory/chartboost-ads/")
		maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
		maven(url = "https://artifact.bytedance.com/repository/pangle")
	}
	// TODO: add
//	dependencies {
//		classpath("com.applovin.quality:AppLovinQualityServiceGradlePlugin:+")
//	}
}
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.compose) apply false
	// TODO: add
//	id("com.google.gms.google-services") version "4.4.3" apply false
//	id("com.google.firebase.crashlytics") version "3.0.6" apply false

}