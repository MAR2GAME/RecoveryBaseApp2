package com.pdffox.adv.use.adv

import android.util.Log
import com.google.android.gms.ads.nativead.NativeAd
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import kotlinx.serialization.Serializable
import org.json.JSONArray
import kotlin.text.clear

object AdvIDs {

	private var ADMOB_BANNER_ID = "ca-app-pub-3615322193850391/4485010266"
	private var ADMOB_INTERSTITIAL_ID = "ca-app-pub-3615322193850391/2830923916"
	private var ADMOB_NATIVE_ID = "ca-app-pub-3615322193850391/5283086610"
	private var ADMOB_OPEN_ID = "ca-app-pub-3615322193850391/9204760570"

	private var ADMOB_NATIVE_IDS = CircularQueue<NativeAdId>(1)

	fun setNativeIDs(nativeAdIds: String) {
		if (nativeAdIds == "") return
		val ids = if (BuildConfig.DEBUG) {
			Log.e("NativeAdIDs", "setNativeIDs: $nativeAdIds" )
			"""
			[
			  {
			    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
			    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
			    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
			  },
			  {
			    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
			    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
			    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
			  },
			  {
			    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
			    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
			    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
			  }
			]
			""".trimIndent()
		} else {
			nativeAdIds
		}

		try {
			// 1. 解析 JSON 字符串
			if (BuildConfig.DEBUG) {
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

	private const val TEST_ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"
	private const val TEST_ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
	private const val TEST_ADMOB_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
	private const val TEST_ADMOB_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

	const val MAX_SDK_KEY = "eitvS9P6OFat9gTtupcVe3qoDdAfksOVZfgZK7WHozH6kOsIcUnT1oOUESIGTxeTlBnTEd7X2ifkeumC_qJqob"
	var MAX_INTERSTITIAL_ID = "5d4877fef083a3c7"
	var MAX_BANNER_ID = "88bb4d0ada02804b"
	var MAX_OPEN_ID = "96a04fb97bc29c16"

	var TopON_APP_ID = "j07254e2b6652158"
	var TopON_APP_KEY = "j23726c55c756ae3c02fed5bcb8e5099d50b3b76d"
	var TopON_INTERSTITIAL_ID = "j07254e2b6652158"
	var TopON_OPEN_ID = "j07254e2b6652158"

	fun setMaxIDs(interstitialAdId: String, bannerAdId: String, openID: String) {
		MAX_INTERSTITIAL_ID = interstitialAdId
		MAX_BANNER_ID = bannerAdId
		MAX_OPEN_ID = openID
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

data class NativeAdContent(val index: Int, var hAd: NativeAd?, var mAd: NativeAd?, var lAd: NativeAd?)

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
