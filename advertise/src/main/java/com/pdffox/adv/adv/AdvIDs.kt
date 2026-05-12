package com.pdffox.adv.adv

import android.util.Log
import com.pdffox.adv.AdMobConfig
import com.pdffox.adv.AdvertiseSdkConfig
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import kotlinx.serialization.Serializable
import org.json.JSONArray

object AdvIDs {

	private var ADMOB_BANNER_ID = Config.sdkConfig.adMob.bannerId
	private var ADMOB_INTERSTITIAL_ID = Config.sdkConfig.adMob.interstitialId
	private var ADMOB_NATIVE_ID = Config.sdkConfig.adMob.nativeId
	private var ADMOB_OPEN_ID = Config.sdkConfig.adMob.openId
	private var TEST_ADMOB_BANNER_ID = Config.sdkConfig.adMob.testBannerId
	private var TEST_ADMOB_INTERSTITIAL_ID = Config.sdkConfig.adMob.testInterstitialId
	private var TEST_ADMOB_NATIVE_ID = Config.sdkConfig.adMob.testNativeId
	private var TEST_ADMOB_OPEN_ID = Config.sdkConfig.adMob.testOpenId
	private var DEBUG_NATIVE_IDS_JSON = Config.sdkConfig.adMob.debugNativeIdsJson

	private var ADMOB_NATIVE_IDS = CircularQueue<NativeAdId>(1)

	fun configure(config: AdvertiseSdkConfig) {
		configureAdMob(config.adMob)
	}

	private fun configureAdMob(config: AdMobConfig) {
		ADMOB_BANNER_ID = config.bannerId
		ADMOB_INTERSTITIAL_ID = config.interstitialId
		ADMOB_NATIVE_ID = config.nativeId
		ADMOB_OPEN_ID = config.openId
		TEST_ADMOB_BANNER_ID = config.testBannerId
		TEST_ADMOB_INTERSTITIAL_ID = config.testInterstitialId
		TEST_ADMOB_NATIVE_ID = config.testNativeId
		TEST_ADMOB_OPEN_ID = config.testOpenId
		DEBUG_NATIVE_IDS_JSON = config.debugNativeIdsJson
	}

	fun setNativeIDs(nativeAdIds: String) {
		if (nativeAdIds == "") return
		val ids = if (com.pdffox.adv.Config.isTest) {
			Log.e("NativeAdIDs", "setNativeIDs: $nativeAdIds" )
			DEBUG_NATIVE_IDS_JSON.trimIndent()
		} else {
			nativeAdIds
		}

		try {
			// 1. 解析 JSON 字符串
			if (com.pdffox.adv.Config.isTest) {
				Log.e("AdvIDs", "setNativeIDs: $ids" )
			}
			val jsonArray = JSONArray(ids)
			val count = jsonArray.length()
			if (count > 0) {
				// 2. 核心修改：重新创建指定容量的队列
				ADMOB_NATIVE_IDS = CircularQueue(count)

				// 3. 循环添加到队列中
				for (i in 0 until count) {
					val jsonObject = jsonArray.getJSONObject(i)
					val highPriceID = jsonObject.optString("highPriceID")
					val midPriceID = jsonObject.optString("midPriceID")
					val lowPriceID = jsonObject.optString("lowPriceID")

					// 入队
					ADMOB_NATIVE_IDS.enqueue(NativeAdId(i, highPriceID, midPriceID, lowPriceID))
				}
			}
		} catch (e: Exception) {
			Log.e("AdvIDs", "Error parsing nativeAdIds: ${e.message}")
		}
	}

	fun getNextNativeAdId(): NativeAdId? {
		val id = ADMOB_NATIVE_IDS.dequeue() ?: return null
		ADMOB_NATIVE_IDS.enqueue(id)
		return id
	}

	fun setAdmobIDs(
		bannerId: String,
		interstitialAdId: String,
		nativeAdId: String,
		openAdId: String,
	) {
		ADMOB_BANNER_ID = bannerId
		ADMOB_INTERSTITIAL_ID = interstitialAdId
		ADMOB_NATIVE_ID = nativeAdId
		ADMOB_OPEN_ID = openAdId
	}

	private fun selectId(testId: String, prodId: String): String {
		return if (Config.isTest) testId else prodId
	}

	fun getAdmobBannerId() = selectId(TEST_ADMOB_BANNER_ID, ADMOB_BANNER_ID)

	fun getAdmobInterstitialId() = selectId(TEST_ADMOB_INTERSTITIAL_ID, ADMOB_INTERSTITIAL_ID)

	fun getAdmobNativeId() = selectId(TEST_ADMOB_NATIVE_ID, ADMOB_NATIVE_ID)

	fun getAdmobOpenId() = selectId(TEST_ADMOB_OPEN_ID, ADMOB_OPEN_ID)
}

data class NativeAdId(val index: Int, val hId: String, val mId: String, val aId: String)

class CircularQueue<T>(private val capacity: Int) {
	// 实际存储数组，大小比容量多1，用于区分空和满
	private val queue = arrayOfNulls<Any>(capacity +1)
	private var front = 0
	private var rear = 0

	// 检查是否已满
	fun isFull(): Boolean = (rear + 1) % (capacity + 1) == front

	// 检查是否为空
	fun isEmpty(): Boolean = front == rear

	// 入队 (Enqueue)
	fun enqueue(element: T): Boolean {
		if (isFull()) return false
		queue[rear] = element
		rear = (rear + 1) % (capacity + 1)
		return true
	}

	// 出队 (Dequeue)
	@Suppress("UNCHECKED_CAST")
	fun dequeue(): T? {
		if (isEmpty()) return null
		val element = queue[front] as T
		queue[front] = null // 释放引用
		front = (front + 1) % (capacity + 1)
		return element
	}

	// 查看队首元素
	@Suppress("UNCHECKED_CAST")
	fun peek(): T? = if (isEmpty()) null else queue[front] as T

	// 新增清空方法，方便重新加载 ID
	fun clear() {
		front = 0
		rear = 0
		queue.fill(null)
	}
}
