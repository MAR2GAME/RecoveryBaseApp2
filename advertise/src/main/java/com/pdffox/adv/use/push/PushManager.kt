package com.pdffox.adv.use.push

import android.util.Log
import cn.thinkingdata.analytics.TDAnalytics
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.AdvCheckManager
import com.pdffox.adv.use.util.PreferenceDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

@Serializable
data class PushData(
	val PackageName: String,
	val Tag: Int,
	val Token: String,
	val Language: String,
	val DistinctId: String,
)

object PushManager {

	private const val TAG = "PushManager"

	var pushToken: String by PreferenceDelegate("pushToken", "")

	fun notifyServerAppExit() {
		CoroutineScope(Dispatchers.IO).launch {
			val client = OkHttpClient.Builder()
				.connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.build()

			val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
			val pushData = PushData(
				PackageName = Config.packageName,
				Tag = AdvCheckManager.params.tag,
				Token = pushToken,
				Language = Locale.getDefault().language,
				TDAnalytics.getDistinctId()
			)
			Log.e(TAG, "notifyServerAppExit: $pushData" )
			val jsonBody = Json.encodeToString(pushData).toRequestBody(mediaType)
			val request = Request.Builder()
				.url(Config.PushUrl)
				.post(jsonBody)
				.build()
			try {
				client.newCall(request).execute().use { response ->
					val body = response.body.string()
					Log.e(TAG, "notifyServerAppExit:  $body")
				}
			} catch (e: Exception) {
				Log.e(TAG, "Exception notifyServerAppExit: $e")
			}
		}
	}

}