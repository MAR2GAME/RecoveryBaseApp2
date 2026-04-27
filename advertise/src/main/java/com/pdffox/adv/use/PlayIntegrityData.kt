package com.pdffox.adv.use

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Root(
	val accountDetails: AccountDetails? = null,
	val appIntegrity: AppIntegrity? = null,
	val deviceIntegrity: DeviceIntegrity? = null,
	val requestDetails: RequestDetails? = null,
)

@Serializable
data class AccountDetails(
	val appLicensingVerdict: String? = null,
)

@Serializable
data class AppIntegrity(
	val appRecognitionVerdict: String? = null,
	val certificateSha256Digest: List<String> = emptyList(),
	val packageName: String? = null,
	val versionCode: String? = null,
)

@Serializable
data class DeviceIntegrity(
	val deviceRecognitionVerdict: List<String> = emptyList(),
)

@Serializable
data class RequestDetails(
	val requestHash: String? = null,
	val requestPackageName: String? = null,
	val timestampMillis: String? = null,
)

private val playIntegrityJson = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
}

fun parseJson(jsonString: String): Root {
	return playIntegrityJson.decodeFromString(jsonString)
}
