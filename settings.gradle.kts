pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		google()
		mavenCentral()
		gradlePluginPortal()
		maven(url = "https://jitpack.io")
		maven(url = "https://maven.singular.net/")
		maven(url = "https://cboost.jfrog.io/artifactory/chartboost-ads/")
		maven(url = "https://artifact.bytedance.com/repository/pangle")
		maven(url = "https://android-sdk.is.com/")
		maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
		maven(url = "https://artifact.bytedance.com/repository/pangle/")
		maven(url = "https://jfrog.anythinktech.com/artifactory/overseas_sdk")

	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven(url = "https://jitpack.io")
		maven(url = "https://repository.liferay.com/nexus/content/repositories/public/")
		maven(url = "https://maven.singular.net/")
		maven(url = "https://cboost.jfrog.io/artifactory/chartboost-ads/")
		maven(url = "https://artifact.bytedance.com/repository/pangle")
		maven(url = "https://android-sdk.is.com/")
		maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
		maven(url = "https://artifact.bytedance.com/repository/pangle/")
		maven(url = "https://jfrog.anythinktech.com/artifactory/overseas_sdk")
	}
}

rootProject.name = "Photo Recovery"
include(":app")
include(":advertise")
//include(":biddingsdk")
