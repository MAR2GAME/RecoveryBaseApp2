package com.pdffox.adv.use.remoteconfig

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.pdffox.adv.use.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.AdConfig
import com.pdffox.adv.use.adv.policy.AdPolicyManager
import com.pdffox.adv.use.adv.policy.data.AdMapping
import com.pdffox.adv.use.adv.policy.data.Config as MappingConfig
import com.pdffox.adv.use.adv.policy.data.parseAdMapping
import com.pdffox.adv.use.notification.NotificationManager
import com.pdffox.adv.use.util.PreferenceUtil
import java.util.Locale

internal data class RoutingContext(
	val useNatureConfig: Boolean,
	val country: String,
	val brand: String
)

internal data class RoutingSelection(
	val topic: String?,
	val adTag: String?,
	val notificationTag: String?,
	val preloadTag: String?
)

internal data class RoutingDefaults(
	val adPolicy: String,
	val notificationConfig: String,
	val preloadConfig: String
)

internal data class ResolvedRouting(
	val topic: String?,
	val adPolicy: String,
	val notificationConfig: String,
	val preloadConfig: String,
	val adTag: String?,
	val notificationTag: String?,
	val preloadTag: String?
) {
	val cacheKey: String = buildString {
		append(topic.orEmpty())
		append('|')
		append(adPolicy.hashCode())
		append('|')
		append(notificationConfig.hashCode())
		append('|')
		append(preloadConfig.hashCode())
	}
}

internal fun shouldUseNatureRouting(
	singularHasResult: Boolean = Config.singularHasResult,
	openReview: Boolean = Config.openReview,
	isNature: Boolean = Config.isNature,
	isGoogleIP: Boolean = Config.isGoogleIP,
	ipCheckHasResult: Boolean = Config.ipCheckHasResult,
	paid0HasResult: Boolean = Config.paid0HasResult,
	paid0: Boolean = Config.paid_0
): Boolean {
	return (openReview && isNature && singularHasResult) ||
		(isGoogleIP && ipCheckHasResult) ||
		(paid0HasResult && paid0)
}

internal fun buildRoutingContext(
	useNatureConfig: Boolean = shouldUseNatureRouting(),
	country: String = Locale.getDefault().country,
	brand: String = Build.BRAND
): RoutingContext {
	return RoutingContext(
		useNatureConfig = useNatureConfig,
		country = country,
		brand = brand
	)
}

internal fun selectRouting(
	adMapping: AdMapping?,
	context: RoutingContext,
	allowTargetedSelection: Boolean = true
): RoutingSelection? {
	if (!allowTargetedSelection || adMapping == null) {
		return null
	}
	val selectedConfig = if (context.useNatureConfig) {
		adMapping.nature_config
	} else {
		adMapping.configs.firstOrNull { item ->
			item.countrys.any { it.equals(context.country, ignoreCase = true) } &&
				item.brands.any { it.equals(context.brand, ignoreCase = true) }
		}?.config
	}
	return selectedConfig?.toRoutingSelection()
}

internal fun resolveRouting(
	selection: RoutingSelection?,
	defaults: RoutingDefaults,
	lookup: (String) -> String
): ResolvedRouting {
	fun resolveValue(tag: String?, fallback: String): String {
		if (tag.isNullOrBlank()) {
			return fallback
		}
		val value = lookup(tag)
		return if (value.isNotBlank()) value else fallback
	}

	return ResolvedRouting(
		topic = selection?.topic?.takeIf { it.isNotBlank() },
		adPolicy = resolveValue(selection?.adTag, defaults.adPolicy),
		notificationConfig = resolveValue(selection?.notificationTag, defaults.notificationConfig),
		preloadConfig = resolveValue(selection?.preloadTag, defaults.preloadConfig),
		adTag = selection?.adTag,
		notificationTag = selection?.notificationTag,
		preloadTag = selection?.preloadTag
	)
}

private fun MappingConfig.toRoutingSelection(): RoutingSelection {
	return RoutingSelection(
		topic = fcm_topic,
		adTag = ad,
		notificationTag = notification,
		preloadTag = preload
	)
}

object RemoteConfigRouting {
	private const val TAG = "RemoteConfigRouting"

	@Volatile
	private var lastAppliedKey: String? = null

	@Synchronized
	fun apply(
		remoteConfig: FirebaseRemoteConfig,
		adMapping: String,
		source: String,
		allowTargetedSelection: Boolean = true
	) {
		val selection = selectRouting(
			adMapping = parseAdMapping(adMapping),
			context = buildRoutingContext(),
			allowTargetedSelection = allowTargetedSelection
		)
		val resolved = resolveRouting(
			selection = selection,
			defaults = RoutingDefaults(
				adPolicy = remoteConfig.getString("ad_policy"),
				notificationConfig = remoteConfig.getString("notification_config"),
				preloadConfig = remoteConfig.getString("adload_config")
			),
			lookup = remoteConfig::getString
		)
		if (resolved.cacheKey == lastAppliedKey) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "apply[$source]: skipped duplicate routing")
			}
			return
		}
		lastAppliedKey = resolved.cacheKey
		if (BuildConfig.DEBUG) {
			PreferenceUtil.commitString("routing_source", source)
			PreferenceUtil.commitString("adTag", resolved.adTag.orEmpty())
			PreferenceUtil.commitString("notificationTag", resolved.notificationTag.orEmpty())
			PreferenceUtil.commitString("preloadTag", resolved.preloadTag.orEmpty())
			Log.e(
				TAG,
				"apply[$source]: topic=${resolved.topic}, adTag=${resolved.adTag}, " +
					"notificationTag=${resolved.notificationTag}, preloadTag=${resolved.preloadTag}"
			)
		}
		resolved.topic?.let(Ads::changeTopic)
		AdConfig.updateConfigFromJson(resolved.preloadConfig)
		NotificationManager.updateNotificationConfig(resolved.notificationConfig)
		AdPolicyManager.setPolicyFromJson(resolved.adPolicy)
	}
}
