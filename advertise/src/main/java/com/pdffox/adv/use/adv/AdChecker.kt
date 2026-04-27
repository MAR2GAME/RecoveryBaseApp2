package com.pdffox.adv.use.adv

import android.util.Log
import com.pdffox.adv.use.adv.AdPool.admobInterPool
import com.pdffox.adv.use.adv.AdPool.admobOpenPool
import com.pdffox.adv.use.adv.AdPool.maxInterPool
import com.pdffox.adv.use.adv.AdPool.maxOpenPool
import com.pdffox.adv.use.adv.AdPool.topOnInterPool
import com.pdffox.adv.use.adv.AdPool.topOnOpenPool
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
		checkExpiredMaxInter()
		checkExpiredAdmobOpen()
		checkExpiredMaxOpen()
	}

	fun checkExpiredAdmobInter() {
		admobInterPool.entries.removeIf { (_, time) ->
			val cur = System.currentTimeMillis()
			Log.e(TAG, "checkExpiredAdmobInter: $cur $time ${AdConfig.adload_cache_time}")
			cur - time > AdConfig.adload_cache_time
		}
	}

	fun checkExpiredMaxInter() {
		maxInterPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}

	fun checkExpiredAdmobOpen() {
		admobOpenPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}

	fun checkExpiredMaxOpen() {
		maxOpenPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}

	fun checkExpiredTopOnOpen() {
		topOnOpenPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}

	fun checkExpiredTopOnInter() {
		topOnInterPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}
}