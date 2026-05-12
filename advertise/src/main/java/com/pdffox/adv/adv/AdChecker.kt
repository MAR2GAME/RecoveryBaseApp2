package com.pdffox.adv.adv

import android.util.Log
import com.pdffox.adv.adv.AdPool.admobInterPool
import com.pdffox.adv.adv.AdPool.admobOpenPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 检查并移除过期的广告
 */
object AdChecker {

	private const val TAG = "AdChecker"

	private val scope = CoroutineScope(Dispatchers.Default)
	private var job: Job? = null

	fun startAutoCheck(intervalMillis: Long = 10 * 60 * 1000) {
		if (job?.isActive == true) return
		job = scope.launch {
			while (isActive) {
				checkAd()
				delay(intervalMillis)
			}
		}
	}

	fun stopAutoCheck() {
		job?.cancel()
		job = null
	}

	fun checkAd() {
		checkExpiredAdmobInter()
		checkExpiredAdmobOpen()
	}

	fun checkExpiredAdmobInter() {
		admobInterPool.entries.removeIf { (_, time) ->
			val cur = System.currentTimeMillis()
			Log.e(TAG, "checkExpiredAdmobInter: $cur $time ${AdConfig.adload_cache_time}")
			cur - time > AdConfig.adload_cache_time
		}
	}

	fun checkExpiredAdmobOpen() {
		admobOpenPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}

}
