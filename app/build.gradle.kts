plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	id("kotlin-parcelize")
	id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
	id("kotlin-kapt")
	// TODO: add
//	id("com.google.gms.google-services")
//	id("com.google.firebase.crashlytics")
//	id("applovin-quality-service")
}

// TODO: add
/*applovin {
	apiKey = "2hze1KfFRFgbkALTN_0sPobCNhMBJc0neplV0tTC2f6SCeQSGpEeN4vx6g5kCxvH9ET0wLre6R4TaSURR4ifNR"
}*/

android {
	namespace = "com.quickrecover.photonvideotool"
	compileSdk = 36

	defaultConfig {
		applicationId = "com.quickrecover.photonvideotool"
		minSdk = 29
		targetSdk = 36
		versionCode = 1
		versionName = "1.0.1"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		setProperty("archivesBaseName", "${applicationId}_${versionName}")
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
		debug {
			isMinifyEnabled = false
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
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
		buildConfig = true
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

	// TODO: add
//	implementation(project(":advertise"))
	implementation(project(":safe"))

}