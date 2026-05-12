package com.pdffox.adv.adv.policy.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class AdMapping(
	val config: Config = Config(),
	val nature_config: Config = Config(),
	val configs: List<ConfigItem> = emptyList()
)

@Serializable
data class Config(
	val ad: String = "",
	val notification: String = "",
	val fcm_topic: String = "all",
	val preload: String = ""
)

@Serializable
data class ConfigItem(
	val countrys: List<String> = emptyList(),
	val brands: List<String> = emptyList(),
	val config: Config = Config()
)

private val adMappingJson = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
	coerceInputValues = true
}

fun parseAdMapping(jsonString: String): AdMapping? {
	if (jsonString.isBlank()) {
		return null
	}
	return runCatching {
		adMappingJson.decodeFromString<AdMapping>(jsonString)
	}.getOrNull()
}
