package com.pdffox.adv.adv

import android.app.Activity
import android.os.Process
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.pdffox.adv.Ads
import com.pdffox.adv.Ads.changeTopic
import com.pdffox.adv.AdvRuntime
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.R
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.adv.policy.data.AdMapping
import com.pdffox.adv.adv.policy.data.parseAdMapping
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.notification.NotificationManager
import com.pdffox.adv.parseJson
import com.pdffox.adv.remoteconfig.RemoteConfig
import com.pdffox.adv.remoteconfig.RemoteConfigRouting
import com.pdffox.adv.util.PreferenceDelegate
import com.pdffox.adv.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.Dns
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.system.exitProcess

class AdvCheckParams {
	var tag: Int by PreferenceDelegate("tag", 0)
	var fromNature: Boolean by PreferenceDelegate("fromNature", false)
	var isFirstOpen: Boolean by PreferenceDelegate("isFirstOpen", true)
	var installTime: Long by PreferenceDelegate("installTime", 0L)
	var limitTime: Long by PreferenceDelegate("limitTime", 0L)
	var backgroundDuration: Long by PreferenceDelegate("backgroundDuration", 30L)
	var showFirstOpenAdv by PreferenceDelegate("ShowFirstOpenAdv", false)

	// 触发插屏和开屏广告位次数
	var times: Int by PreferenceDelegate("times", 0)
	// 展示插屏广告次数
	var interTimes: Int by PreferenceDelegate("interTimes", 0)
	// 展示Banner广告次数
	var bannerTimes: Int by PreferenceDelegate("bannerTimes", 0)
	// 展示开屏广告次数
	var openTimes: Int by PreferenceDelegate("openTimes", 0)

	fun toData(areaKey: String): AdvCheckParamsData {
		return AdvCheckParamsData(
			FromNature = fromNature,
			IsFirstOpen = isFirstOpen,
			InstallTime = installTime,
			InterTimes = interTimes,
			BannerTimes = bannerTimes,
			OpenTimes = openTimes,
			PackageName = currentPackageName(),
			AreaKey = areaKey,
			Times = times
		)
	}

	private fun currentPackageName(): String {
		Config.packageName.takeIf { it.isNotBlank() }?.let { return it }
		return AdvRuntime.currentPackageName()
	}
}

@Serializable
data class AdvCheckParamsData(
	val FromNature: Boolean,
	val IsFirstOpen: Boolean,
	val InstallTime: Long,
	val InterTimes: Int,
	val BannerTimes: Int,
	val OpenTimes: Int,
	val PackageName: String,
	val AreaKey: String,
	val Times: Int
)

@Serializable
data class CheckAdvResponse(
	val Code: Int,
	val time: String,
	val Msg: String,
	val CanPlay: Boolean,
	val Tag: Int,
	val LimitTime: Long,
	val BackgroundDuration: Long
)

@Serializable
data class AppConfigResponse(
	val Code: Int,
	val time: String,
	val Msg: String,
	val ShowFirstOpenAdv: Boolean,
	val AdStrategy: Int,
	val BackgroundDuration: Long,
)

@Serializable
data class IpGeoDetail(
	val ip: String?,
	val longitude: Double?,
	val latitude: Double?,
	val asn: String?,
	val isp: String?
)

@Serializable
data class CidrRange(
	val address: ByteArray,
	val prefixLength: Int
) {
	fun contains(target: ByteArray): Boolean {
		if (target.size != address.size) return false
		var remaining = prefixLength
		var index = 0
		while (remaining >= 8) {
			if (address[index] != target[index]) return false
			remaining -= 8
			index++
		}
		if (remaining > 0) {
			val mask = (0xFF shl (8 - remaining)) and 0xFF
			val lhs = address[index].toInt() and mask
			val rhs = target[index].toInt() and mask
			return lhs == rhs
		}
		return true
	}
}

const val CODE_LIMIT_ADV_MAX = 401
const val CODE_LIMIT_ADV_LIMIT = 402
object AdvCheckManager {
	private const val TAG = "AdvCheck"
	private const val PREF_IP_INFO_CHECKED = "pref_ip_info_checked"
	private const val PREF_IP_INFO_RESULT = "pref_ip_info_result"

	var params: AdvCheckParams = AdvCheckParams()
	private val cloudCidrs by lazy { loadCidrsFromRaw(Config.resourceConfig.cloudCidrsRawResId) }
	private val googleCidrs by lazy { loadCidrsFromRaw(Config.resourceConfig.googleCidrsRawResId) }
	@Volatile
	private var ipInfoCacheLoaded = false
	@Volatile
	private var ipInfoCachedResult: String? = null

	private fun checkIpInfoLegacy(): String? {
		PreferenceUtil.init(AdvRuntime.application)
		if (ipInfoCacheLoaded) {
			return ipInfoCachedResult
		}
		if (PreferenceUtil.getBoolean(PREF_IP_INFO_CHECKED, false)) {
			val cachedResult = PreferenceUtil.getString(PREF_IP_INFO_RESULT, null)
			ipInfoCachedResult = cachedResult
			ipInfoCacheLoaded = true
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "checkIpInfo: use cached result = $cachedResult")
			}
			return cachedResult
		}
		val result = runCatching {
			val client = OkHttpClient()
			val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
			val emptyBody = "".toRequestBody(mediaType)

			val request = Request.Builder()
				.url(Config.IPInfoUrl)
				.post(emptyBody)
				.build()

			client.newCall(request).execute().use { response ->
				if (!response.isSuccessful) throw IllegalStateException("Unexpected code $response")
				val result = response.body.string()
				val parsed = parseIpInfoPayload(result) ?: return@use result
				val ipValue = parsed.ip ?: return@use result
					ThinkingAttr.setUserOnceAttr(ThinkingAttr.ip_info,
						"IPInfo ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}"
					)
					Config.ipCheckHasResult = true
					val inCloud = isIpInCidrs(ipValue, cloudCidrs)
					val inGoogle = isIpInCidrs(ipValue, googleCidrs)
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "checkIpInfo: ip=$ipValue inCloud=$inCloud inGoogle=$inGoogle")
					}
					if (inCloud || inGoogle) {
						Config.isGoogleIP = true
					}
					if (parsed.isp?.contains("google", ignoreCase = true) == true) {
						Log.e(TAG, "checkIpInfo: isp为Google")
						Config.isGoogleIP = true
					}
					if (parsed.asn?.contains("15169") == true) {
						Log.e(TAG, "checkIpInfo: asn为15169")
						Config.isGoogleIP = true
					}
					if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
						val remoteConfig = Firebase.remoteConfig
						val adMapping = remoteConfig.getString("ad_mapping")
						RemoteConfigRouting.apply(
							remoteConfig = remoteConfig,
							adMapping = adMapping,
							source = "AdvCheckManager.checkIpInfo"
						)
					} else {
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "checkIpInfo: RemoteConfig还未更新先不处理" )
						}
					}
				if (com.pdffox.adv.Config.isTest) {
					Log.e(
						TAG,
						"IP detail -> ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}"
					)
					return "IP detail -> ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}"
				} else {
					return "IP detail failed to parse"
				}
			}

		}.onFailure {
			Log.e(TAG, "checkIpInfo error", it)
		}.getOrNull()
		PreferenceUtil.commitBoolean(PREF_IP_INFO_CHECKED, true)
		PreferenceUtil.commitString(PREF_IP_INFO_RESULT, result)
		ipInfoCachedResult = result
		ipInfoCacheLoaded = true
		return result
	}

	fun getIpInfoV2(): String? {
		if (!Config.isServerEnabled || Config.IPInfoV2Url.isBlank()) {
			return null
		}
		PreferenceUtil.init(AdvRuntime.application)
		if (ipInfoCacheLoaded) {
			return ipInfoCachedResult
		}
		if (PreferenceUtil.getBoolean(PREF_IP_INFO_CHECKED, false)) {
			val cachedResult = PreferenceUtil.getString(PREF_IP_INFO_RESULT, null)
			ipInfoCachedResult = cachedResult
			ipInfoCacheLoaded = true
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "getIpInfoV2: use cached result = $cachedResult")
			}
			return cachedResult
		}
		val result = runCatching {
			val client = OkHttpClient()
			val request = Request.Builder()
				.url(Config.IPInfoV2Url)
				.get()
				.build()

			client.newCall(request).execute().use { response ->
				if (!response.isSuccessful) throw IllegalStateException("Unexpected code $response")
				val result = response.body.string()
				val parsed = parseIpInfoV2Detail(result)
				return@use applyIpInfoResult(parsed, "AdvCheckManager.getIpInfoV2")
			}

		}.onFailure {
			Log.e(TAG, "getIpInfoV2 error", it)
		}.getOrNull()
		PreferenceUtil.commitBoolean(PREF_IP_INFO_CHECKED, true)
		PreferenceUtil.commitString(PREF_IP_INFO_RESULT, result)
		ipInfoCachedResult = result
		ipInfoCacheLoaded = true
		return result
	}

	private fun parseIp(raw: String): String? {
		return runCatching {
			val json = JSONObject(raw)
			json.optString("Ip").takeIf { it.isNotBlank() }
		}.getOrNull()
	}

	private fun parseIpInfoV2Detail(raw: String): IpGeoDetail? {
		return parseIpInfoPayload(raw)
	}

	private fun parseIpInfoPayload(raw: String): IpGeoDetail? {
		val parsedDetail = runCatching {
			val root = JSONObject(raw)
			val ip = root.optString("Ip").takeIf { it.isNotBlank() }
			val locationInfo = when (val value = root.opt("location_info")) {
				is JSONObject -> value.toString()
				is String -> value.takeIf { it.isNotBlank() }
				else -> null
			}
			val parsed = locationInfo?.let(::parseDetailFields) ?: parseDetailFields(raw)
			parsed?.let { detail ->
				if (detail.ip == null && ip != null) {
					detail.copy(ip = ip)
				} else {
					detail
				}
			}
		}.getOrNull()
		if (parsedDetail != null) {
			return parsedDetail
		}
		val ip = parseIp(raw) ?: return null
		return IpGeoDetail(ip = ip, longitude = null, latitude = null, asn = null, isp = null)
	}

	private fun parseDetailFields(detailJson: String): IpGeoDetail? {
		return runCatching {
			val json = JSONObject(detailJson)
			val ip = json.optString("ip").takeIf { it.isNotBlank() }
			val longitude = json.optDouble("longitude", Double.NaN).takeIf { !it.isNaN() }
			val latitude = json.optDouble("latitude", Double.NaN).takeIf { !it.isNaN() }
			val asn = json.optString("asn").takeIf { it.isNotBlank() }
			val isp = json.optString("isp").takeIf { it.isNotBlank() }
			IpGeoDetail(ip, longitude, latitude, asn, isp)
		}.getOrNull()
	}

	private fun applyIpInfoResult(parsed: IpGeoDetail?, source: String): String {
		if (parsed != null) {
			ThinkingAttr.setUserOnceAttr(
				ThinkingAttr.ip_info,
				"IPInfo ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}"
			)
			Config.ipCheckHasResult = true
			val ipValue = parsed.ip
			if (ipValue != null) {
				val inCloud = isIpInCidrs(ipValue, cloudCidrs)
				val inGoogle = isIpInCidrs(ipValue, googleCidrs)
				if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "getIpInfoV2: ip=$ipValue inCloud=$inCloud inGoogle=$inGoogle")
				}
				if (inCloud || inGoogle) {
					Config.isGoogleIP = true
				}
			}
			if (parsed.isp?.contains("google", ignoreCase = true) == true) {
				Log.e(TAG, "getIpInfoV2: isp为Google")
				Config.isGoogleIP = true
			}
			if (parsed.asn?.contains("15169") == true) {
				Log.e(TAG, "getIpInfoV2: asn为15169")
				Config.isGoogleIP = true
			}
			if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
				val remoteConfig = Firebase.remoteConfig
				val adMapping = remoteConfig.getString("ad_mapping")
				RemoteConfigRouting.apply(
					remoteConfig = remoteConfig,
					adMapping = adMapping,
					source = source
				)
			} else if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "getIpInfoV2: RemoteConfig还未更新先不处理")
			}
		}
		if (com.pdffox.adv.Config.isTest && parsed != null) {
			val detailSummary =
				"IP detail -> ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}"
			Log.e(TAG, detailSummary)
			return detailSummary
		}
		return "IP detail failed to parse"
	}

	private fun isIpInCidrs(ip: String, cidrs: List<CidrRange>): Boolean {
		val target = try {
			InetAddress.getByName(ip).address
		} catch (e: UnknownHostException) {
			Log.e(TAG, "isIpInCidrs: invalid ip $ip", e)
			return false
		}
		return cidrs.any { it.contains(target) }
	}

	private fun loadCidrsFromRaw(resId: Int): List<CidrRange> {
		val context = AdvRuntime.application
		return runCatching {
			context.resources.openRawResource(resId).bufferedReader().use { reader ->
				val root = JSONObject(reader.readText())
				val prefixes = root.optJSONArray("prefixes") ?: JSONArray()
				buildList {
					for (i in 0 until prefixes.length()) {
						val entry = prefixes.optJSONObject(i) ?: continue
						entry.optString("ipv4Prefix").takeIf { it.isNotBlank() }?.let { cidr ->
							parseCidr(cidr)?.let { add(it) }
						}
						entry.optString("ipv6Prefix").takeIf { it.isNotBlank() }?.let { cidr ->
							parseCidr(cidr)?.let { add(it) }
						}
					}
				}
			}
		}.onFailure {
			Log.e(TAG, "loadCidrsFromRaw failed", it)
		}.getOrElse { emptyList() }
	}

	private fun parseCidr(cidr: String): CidrRange? {
		val parts = cidr.split("/")
		if (parts.size != 2) return null
		val prefix = parts[1].toIntOrNull() ?: return null
		return try {
			val addr = InetAddress.getByName(parts[0]).address
			CidrRange(addr, prefix)
		} catch (e: Exception) {
			Log.e(TAG, "parseCidr failed for $cidr", e)
			null
		}
	}

	fun checkAdv(areaKey: String) : Boolean {
		if (com.pdffox.adv.Config.isTest) return true
		if (!Config.isServerEnabled || Config.CheckUrl.isBlank()) return true
		params.times ++
		if (params.limitTime > System.currentTimeMillis()){
			return false
		}
		val client = OkHttpClient.Builder()
			.connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
			.readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
			.writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
			.build()

		val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
		val dataParams = params.toData(areaKey)
		Log.e(TAG, "checkAdv: $areaKey $dataParams")
		val jsonBody = Json.encodeToString(dataParams).toRequestBody(mediaType)
		val request = Request.Builder()
			.url(Config.CheckUrl)
			.post(jsonBody)
			.build()
		try {
			client.newCall(request).execute().use { response ->
				val body = response.body.string()
				Log.e(TAG, "checkAdv: $areaKey $body")
				if (response.isSuccessful) {
					val resp = Json.decodeFromString<CheckAdvResponse>(body)
					Log.e(TAG, "checkAdv: $areaKey success , CanPlay: ${resp.CanPlay}")
					params.tag = resp.Tag

					if (resp.BackgroundDuration > 0) {
						params.backgroundDuration = resp.BackgroundDuration
					}
					if (resp.Code == CODE_LIMIT_ADV_MAX){
						params.times = 0
						params.interTimes = 0
						params.bannerTimes = 0
						params.openTimes = 0
						params.limitTime = System.currentTimeMillis() + resp.LimitTime * 1000
						EventBus.getDefault().post(AdvLimitedEvent())
					} else if (resp.Code == CODE_LIMIT_ADV_LIMIT){
						params.times = 0
						params.interTimes = 0
						params.bannerTimes = 0
						params.openTimes = 0
						params.limitTime = System.currentTimeMillis() + resp.LimitTime * 1000
						EventBus.getDefault().post(AdvLimitedEvent())
					}
					return resp.CanPlay
				} else {
					return false
				}
			}
		} catch (e: Exception) {
			Log.e(TAG, "checkAdv: $areaKey $e")
			return false
		}

	}

	// 自定义 DNS 逻辑
	object IPv4FirstDns : Dns {
		override fun lookup(hostname: String): List<InetAddress> {
			val addresses = Dns.SYSTEM.lookup(hostname)
			// 将 IPv4 地址排在前面，或者只保留 IPv4
			return addresses.sortedBy { if (it is Inet4Address) 0 else 1 }
		}
	}
	fun checkToken(token: String) {
		if (!Config.isServerEnabled || Config.ParseTokenUrl.isBlank() || Config.sdkConfig.server.parseTokenKey.isBlank()) {
			return
		}
		Log.e(TAG, "checkToken: $token", )
		val client = OkHttpClient
			.Builder()
			.dns(IPv4FirstDns)
			.build()
		val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
		val params = JSONObject().apply {
			put("Key", Config.sdkConfig.server.parseTokenKey)
			put("PackageName", AdvRuntime.currentPackageName())
			put("token", token)
		}
		Log.e(TAG, "checkToken: $token $params")
		val jsonBody = params.toString().toRequestBody(mediaType)
		val request = Request.Builder()
			.url(Config.ParseTokenUrl)
			.post(jsonBody)
			.build()
		try {
			client.newCall(request).execute().use { response ->
				val body = response.body.string()
				Log.e(TAG, "checkToken: $body")
				if (response.isSuccessful) {
					val json = JSONObject(body)
					val dataString = json.getString("data")
					Log.e(TAG, "checkToken: dataString = $dataString")
					val playIntegrityData = parseJson(dataString)

//					val hashCode = playIntegrityData.requestDetails.requestHash
//					val requestHash = PreferenceUtil.getString("requestHash", "")
//					if (com.pdffox.adv.Config.isTest) {
//						Log.e(TAG, "checkToken: hashCode = $hashCode, requestHash = $requestHash")
//					}
//					if (hashCode != requestHash){
//						Log.e(TAG, "checkToken: hashCode != requestHash")
//						// TODO: Hash不对 请求被篡改
//					}
//					val requestPackageName = playIntegrityData.requestDetails.requestPackageName
//					if (com.pdffox.adv.Config.isTest) {
//						Log.e(TAG, "checkToken: requestPackageName = $requestPackageName")
//					}
//					if (requestPackageName != AdvRuntime.currentPackageName()){
//						Log.e(TAG, "checkToken: requestPackageName != packageName")
//						// TODO: PackageName不对 请求被篡改
//					}
//
//					if (!playIntegrityData.deviceIntegrity.deviceRecognitionVerdict.contains("MEETS_DEVICE_INTEGRITY")){
//						Log.e(TAG, "checkToken: deviceRecognitionVerdict != MEETS_DEVICE_INTEGRITY")
//						// TODO: DeviceRecognitionVerdict不对 不在认证的 Android 设备上运行
//					}

					val appIntegrity = playIntegrityData.appIntegrity
					if (appIntegrity == null) {
						Log.w(TAG, "checkToken: missing appIntegrity payload")
						return
					}
					if (appIntegrity.packageName.isNullOrBlank() || appIntegrity.versionCode.isNullOrBlank()) {
						Log.w(TAG, "checkToken: incomplete appIntegrity payload, skip enforcement")
						return
					}
					if (appIntegrity.appRecognitionVerdict == "UNRECOGNIZED_VERSION"){
						PreferenceUtil.commitBoolean("appRecognitionVerdict", false)
						Log.e(TAG, "checkToken: appRecognitionVerdict != PLAY_RECOGNIZED")
						// TODO: AppRecognitionVerdict不对 APP完整性存疑
						AdvRuntime.finishCurrentActivity()
						Process.killProcess(Process.myPid())
						exitProcess(0)
					} else {
						PreferenceUtil.commitBoolean("appRecognitionVerdict", true)
					}

				}
			}
		} catch (e: Exception) {
			Log.e(TAG, "checkToken: ", e)
		}
	}

	suspend fun getAppConfig() {
		if (!Config.isServerEnabled || Config.AppConfigUrl.isBlank()) {
			return
		}
		withContext(Dispatchers.IO) {
			val client = OkHttpClient.Builder()
				.connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.build()

			val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
			val dataParams = params.toData("")
			Log.e(TAG, "getAppConfig: $dataParams")
			val jsonBody = Json.encodeToString(dataParams).toRequestBody(mediaType)
			val request = Request.Builder()
				.url(Config.AppConfigUrl)
				.post(jsonBody)
				.build()
			try {
				client.newCall(request).execute().use { response ->
					val body = response.body.string()
					Log.e(TAG, "getAppConfig result:  $body")
					if (response.isSuccessful) {
						val resp = Json.decodeFromString<AppConfigResponse>(body)
						params.tag = resp.AdStrategy
						params.showFirstOpenAdv = resp.ShowFirstOpenAdv
						params.backgroundDuration = resp.BackgroundDuration
						Log.e(TAG, "getAppConfig result:  ${params.showFirstOpenAdv}")
					}
				}
			} catch (e: Exception) {
				Log.e(TAG, "getAppConfig: $e")
			}
		}
	}

}
