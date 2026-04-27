package com.pdffox.adv.use.adv

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.interstitial.api.TUInterstitialListener
import com.thinkup.splashad.api.TUSplashAd
import com.thinkup.splashad.api.TUSplashAdExtraInfo
import com.thinkup.splashad.api.TUSplashAdListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlinx.coroutines.async

/**
 * 广告加载器
 */
object AdLoader {

	private const val TAG = "AdLoader"

	fun loadOpen(context: Context) {
		when (Config.showAdPlatform) {
			LogAdParam.ad_platform_bidding -> {
				fillAdmobOpenPool(context)
				fillMaxOpenPool(context)
				fillTopOnPool(context)
			}
			LogAdParam.ad_platform_admob -> {
				fillAdmobOpenPool(context)
			}
			LogAdParam.ad_platform_max -> {
				fillMaxOpenPool(context)
			}
			LogAdParam.ad_platform_topon -> {
				fillTopOnPool(context)
			}
		}
	}

	private fun fillAdmobOpenPool(context: Context) {
		AdChecker.checkExpiredAdmobOpen()
		val loadSize = AdConfig.adload_poolsize_open - AdPool.admobOpenPool.size - AdPool.admobOpenIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				loadAdmobOpen(context)
			}
		}
	}

	private fun fillMaxOpenPool(context: Context) {
		AdChecker.checkExpiredMaxOpen()
		val loadSize = AdConfig.adload_poolsize_open - AdPool.maxOpenPool.size - AdPool.maxOpenIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				loadMaxOpen(context)
			}
		}
	}

	private fun fillTopOnPool(context: Context) {
		AdChecker.checkExpiredTopOnOpen()
		val loadSize = AdConfig.adload_poolsize_open - AdPool.topOnOpenPool.size - AdPool.toponOpenIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				loadTopOnOpen(context)
			}
		}
	}

	fun loadInter(context: Context) {
		when (Config.showAdPlatform) {
			LogAdParam.ad_platform_bidding -> {
				fillAdmobInterPool(context)
				fillMaxInterPool(context)
				fillTopOnInterPool(context)
			}
			LogAdParam.ad_platform_admob -> {
				fillAdmobInterPool(context)
			}
			LogAdParam.ad_platform_max -> {
				fillMaxInterPool(context)
			}
			LogAdParam.ad_platform_topon -> {
				fillTopOnInterPool(context)
			}
		}
	}

	private fun fillAdmobInterPool(context: Context) {
		AdChecker.checkExpiredAdmobInter()
		val loadSize = AdConfig.adload_poolsize_inter - AdPool.admobInterPool.size - AdPool.admobInterIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				Log.e(TAG, "loadInter: loadAdmobInter 首次开始广告预加载")
				loadAdmobInter(context)
			}
		}
	}

	private fun fillMaxInterPool(context: Context) {
		AdChecker.checkExpiredMaxInter()
		val loadSize = AdConfig.adload_poolsize_inter - AdPool.maxInterPool.size - AdPool.maxInterIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				loadMaxInter(context)
			}
		}
	}

	private fun fillTopOnInterPool(context: Context) {
		AdChecker.checkExpiredTopOnInter()
		val loadSize = AdConfig.adload_poolsize_inter - AdPool.topOnInterPool.size - AdPool.toponInterIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				loadTopOnInter(context)
			}
		}
	}

	fun fillNativePool(context: Context, onAdGroupLoaded: (() -> Unit)? = null) {
		Log.e(TAG, "fillNativePool: " )
		CoroutineScope(Dispatchers.Main).launch {
			val ids = AdvIDs.getNextNativeAdId() ?: return@launch

			if (AdPool.admobNativePool.size > AdConfig.adload_poolsize_native) return@launch

			if (AdPool.admobNativeIsLoadingNum > 3) return@launch

			Log.e(TAG, "开始并行加载原生广告组: ${ids.index}")

			AdPool.admobNativeIsLoadingNum++

			// 使用 current scope (this) 来调用 async
			// 这样 async 就能被正确识别
			val hDeferred = async(Dispatchers.IO) { loadNativeAdSuspended(context, ids.hId) }
			val mDeferred = async(Dispatchers.IO) { loadNativeAdSuspended(context, ids.mId) }
			val aDeferred = async(Dispatchers.IO) { loadNativeAdSuspended(context, ids.aId) }

			// 等待所有任务完成
			val hAd = hDeferred.await()
			val mAd = mDeferred.await()
			val lAd = aDeferred.await()

			val nativeContent = NativeAdContent(ids.index, hAd, mAd, lAd)

			AdPool.admobNativeIsLoadingNum--

			if (hAd != null || mAd != null || lAd != null) {
				// 原生广告组入库
				AdPool.putAdmobNative(nativeContent)
				Log.e(TAG, "原生广告组 ${ids.index} 预加载完成并入库，广告池库存 ${AdPool.admobNativePool.size}")
				onAdGroupLoaded?.invoke()
			} else {
				Log.e(TAG, "原生广告组 ${ids.index} 全部加载失败")
			}

		}
	}

	private fun loadAdmobOpen(context: Context, retryCount: Int = 0) {
		if (AdPool.admobOpenPool.size + AdPool.admobOpenIsLoadingNum >= AdConfig.adload_poolsize_open) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.admobOpenIsLoadingNum++
		Log.e(TAG, "loadAdmobOpen: 开始预加载广告")
		AppOpenAd.load(
			context,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(admobAppOpenAd: AppOpenAd) {
					Log.e(TAG, "loadAdmobOpen: 预加载广告成功")
					isLoaded = true
					AdPool.admobOpenIsLoadingNum--
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to "PreLoad",
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to true,
						)
					)
					AdPool.putAdmobOpen(admobAppOpenAd)
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					Log.e(TAG, "onAdFailedToLoad: 预加载广告失败" + loadAdError.message)
					AdPool.admobOpenIsLoadingNum--
					if (retryCount < AdConfig.adload_retry_num) {
						android.os.Handler(context.mainLooper).postDelayed({
							if (!isLoaded) {
								Log.e(TAG, "loadAdmobOpen fail, retrying...")
								if (retryCount < AdConfig.adload_retry_num) {
									Log.e(TAG, "loadInter: loadAdmobInter 广告加载超时重试")
									loadAdmobOpen(context, retryCount + 1)
								} else {
									Log.e(TAG, "loadAdmobOpen reached max retry count on fail.")
								}
							}
						}, 1000)
					} else {
						Log.e(TAG, "loadAdmobOpen reached max retry count.")
					}
				}
			},
		)
		if (retryCount == 0) {
			android.os.Handler(context.mainLooper).postDelayed({
				if (!isLoaded) {
					Log.e(TAG, "loadAdmobOpen timeout, retrying...")
					if (retryCount < AdConfig.adload_retry_num) {
						Log.e(TAG, "loadInter: loadAdmobInter 广告加载超时重试")
						loadAdmobOpen(context, retryCount + 1)
					} else {
						Log.e(TAG, "loadAdmobOpen reached max retry count on timeout.")
					}
				}
			}, AdConfig.adload_max_time)
		}
	}

	private fun loadAdmobInter(context: Context, retryCount: Int = 0) {
		if (AdPool.admobInterPool.size + AdPool.admobInterIsLoadingNum >= AdConfig.adload_poolsize_inter) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.admobInterIsLoadingNum++
		Log.e(TAG, "loadAdmobInter: 开始预加载广告")
		InterstitialAd.load(
			context,
			AdvIDs.getAdmobInterstitialId(),
			AdRequest.Builder().build(),
			object : InterstitialAdLoadCallback() {
				override fun onAdLoaded(adInter: InterstitialAd) {
					isLoaded = true
					AdPool.admobInterIsLoadingNum--
					Log.e(TAG, "预加载广告成功 loadAdmobInter Ad was loaded.")
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to "PreLoad",
							LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
							LogAdParam.ad_source to (adInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
							LogAdParam.ad_preload to true,
						)
					)
					AdPool.putAdmobInter(adInter)
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					Log.e(TAG, "预加载广告失败 $adError.message")
					AdPool.admobInterIsLoadingNum--
					if (retryCount < AdConfig.adload_retry_num) {
						android.os.Handler(context.mainLooper).postDelayed({
							if (!isLoaded) {
								Log.e(TAG, "loadAdmobInter fail, retrying...")
								if (retryCount < AdConfig.adload_retry_num) {
									Log.e(TAG, "loadInter: loadAdmobInter 广告加载超时重试")
									loadAdmobInter(context, retryCount + 1)
								} else {
									Log.e(TAG, "loadAdmobInter reached max retry count on fail.")
								}
							}
						}, 1000)
					} else {
						Log.e(TAG, "loadAdmobInter reached max retry count.")
					}
				}
			},
		)
		if (retryCount == 0) {
			android.os.Handler(context.mainLooper).postDelayed({
				if (!isLoaded) {
					Log.e(TAG, "loadAdmobInter timeout, retrying...")
					if (retryCount < AdConfig.adload_retry_num) {
						Log.e(TAG, "loadInter: loadAdmobInter 广告加载超时重试")
						loadAdmobInter(context, retryCount + 1)
					} else {
						Log.e(TAG, "loadAdmobInter reached max retry count on timeout.")
					}
				}
			}, AdConfig.adload_max_time)
		}
	}

	// 加载原生广告
	@SuppressLint("MissingPermission")
	fun loadNativeAD(context: Context, nativeAdId: String, onAdLoaded: (NativeAd?) -> Unit) {
		Log.e(TAG, "loadNativeAD: 开始加载原生广告 nativeAdId = $nativeAdId" )
		CoroutineScope(Dispatchers.IO).launch {
			LogUtil.log(
				LogAdData.ad_start_loading,
				mapOf(
					LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
					LogAdParam.ad_areakey to "PreLoad",
					LogAdParam.ad_format to LogAdParam.ad_format_native,
					LogAdParam.ad_unit_name to nativeAdId,
					LogAdParam.ad_preload to true,
				)
			)
			val adLoader =
				com.google.android.gms.ads.AdLoader.Builder(context, nativeAdId)
					.forNativeAd { nativeAd ->
						// 1. 获取标题 (Headline)
						val headline = nativeAd.headline
						// 2. 获取正文/描述 (Body)
						val body = nativeAd.body
						// 3. 获取行动呼吁按钮文字 (Call to Action) - 例如 "安装", "打开"
						val callToAction = nativeAd.callToAction
						// 4. 获取图标 (Icon)
						val icon = nativeAd.icon // 返回 NativeAd.Image 对象
						// 5. 获取评分 (Star Rating)
						val starRating = nativeAd.starRating // Double 类型
						// 6. 获取店铺信息 (Store) - 例如 "Google Play"
						val store = nativeAd.store
						// 7. 获取价格 (Price)
						val price = nativeAd.price
						// 8. 获取广告商名称 (Advertiser)
						val advertiser = nativeAd.advertiser
						// 9. 获取多媒体内容 (MediaContent) - 用于 MediaView 展示视频或大图
						val mediaContent = nativeAd.mediaContent
						Log.e(TAG, "loadNativeAD: 加载成功  nativeAdId = $nativeAdId \n $headline \n $body \n $callToAction \n $icon \n $starRating \n $store \n $price \n $advertiser \n $mediaContent")
						Handler(Looper.getMainLooper()).post {
							onAdLoaded(nativeAd)
						}
					}
					.withAdListener(
						object : AdListener() {
							override fun onAdFailedToLoad(adError: LoadAdError) {
								Log.e(TAG, "loadNativeAD: 加载失败  nativeAdId = $nativeAdId $adError")
								Handler(Looper.getMainLooper()).post {
									onAdLoaded(null)
								}
							}
						}
					)
					// Use the NativeAdOptions.Builder class to specify individual options settings.
					.withNativeAdOptions(NativeAdOptions.Builder().build())
					.build()
			adLoader.loadAd(AdRequest.Builder().build())
		}
	}

	// 使用 suspendCancellableCoroutine 将回调转换为挂起函数
	suspend fun loadNativeAdSuspended(context: Context, adId: String): NativeAd? =
		kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
			loadNativeAD(context, adId) { ad ->
				// 检查协程是否仍然活跃（未被取消）
				if (continuation.isActive) {
					// 使用不带 onCancellation 参数的 resume
					continuation.resume(ad)
				} else {
					// 如果协程已经取消，但广告加载回来了，需要销毁它防止泄漏
					ad?.destroy()
				}
			}

			// 注册取消监听：如果外部协程被取消，立即销毁正在加载或已加载的广告
			continuation.invokeOnCancellation {
				// 注意：这里可能无法直接拿到 ad 对象，
				// 但 loadNativeAD 内部如果持有 nativeAd 引用，
				// 最佳实践是在此处处理相关的清理逻辑
			}
		}

	private fun loadMaxOpen(context: Context, retryCount: Int = 0) {
		if (AdPool.maxOpenPool.size + AdPool.maxOpenIsLoadingNum >= AdConfig.adload_poolsize_open) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.maxOpenIsLoadingNum++
		Log.e(TAG, "loadMaxOpen: 开始预加载广告")
		MaxAppOpenAd(AdvIDs.MAX_OPEN_ID).apply {
			this.setListener(object : MaxAdListener {
				override fun onAdLoaded(maxAd: MaxAd) {
					Log.e(TAG, "预加载广告成功 loadMaxOpen Ad was loaded.")
					isLoaded = true
					AdPool.maxOpenIsLoadingNum--
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to "PreLoad",
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to true,
						)
					)
					AdPool.putMaxOpen(maxAd as MaxAppOpenAd)
				}
				override fun onAdLoadFailed(p0: String, p1: MaxError) {
					Log.e(TAG, "预加载广告失败 onAdLoadFailed: $p0, $p1")
					AdPool.maxOpenIsLoadingNum--
					if (retryCount < AdConfig.adload_retry_num) {
						android.os.Handler(context.mainLooper).postDelayed({
							if (!isLoaded) {
								Log.e(TAG, "loadMaxOpen fail, retrying...")
								if (retryCount < AdConfig.adload_retry_num) {
									Log.e(TAG, "loadMaxOpen: loadMaxOpen 广告加载超时重试")
									loadMaxOpen(context, retryCount + 1)
								} else {
									Log.e(TAG, "loadMaxOpen reached max retry count on fail.")
								}
							}
						}, 1000)
					} else {
						Log.e(TAG, "loadMaxOpen reached max retry count.")
					}
				}
				override fun onAdDisplayed(maxAd: MaxAd) {}
				override fun onAdHidden(maxAd: MaxAd) {}
				override fun onAdClicked(maxAd: MaxAd) {}
				override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {}
			})
			this.loadAd()
		}
		if (retryCount == 0) {
			android.os.Handler(context.mainLooper).postDelayed({
				if (!isLoaded) {
					Log.e(TAG, "loadMaxOpen timeout, retrying...")
					if (retryCount < AdConfig.adload_retry_num) {
						Log.e(TAG, "loadInter: loadMaxOpen 广告加载超时重试")
						loadMaxOpen(context, retryCount + 1)
					} else {
						Log.e(TAG, "loadMaxOpen reached max retry count on timeout.")
					}
				}
			}, AdConfig.adload_max_time)
		}
	}

	private fun loadMaxInter(context: Context, retryCount: Int = 0) {
		if (AdPool.maxInterPool.size + AdPool.maxInterIsLoadingNum >= AdConfig.adload_poolsize_inter) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
				LogAdParam.ad_preload to true,
			)
		)

		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.maxInterIsLoadingNum++
		Log.e(TAG, "loadMaxInter: 开始预加载广告")
		val interstitialAd = MaxInterstitialAd(AdvIDs.MAX_INTERSTITIAL_ID)
		interstitialAd.setListener(object : MaxAdListener {
			override fun onAdLoaded(maxAd: MaxAd) {
				Log.e(TAG, "预加载广告成功 loadMaxInter Ad was loaded.")
				isLoaded = true
				AdPool.maxInterIsLoadingNum--
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to "PreLoad",
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
						LogAdParam.ad_preload to true,
					)
				)
				AdPool.putMaxInter(maxAd as MaxInterstitialAd)
			}
			override fun onAdLoadFailed(s: String, maxError: MaxError) {
				Log.e(TAG, "预加载广告失败 onAdLoadFailed: $s, $maxError")
				AdPool.maxInterIsLoadingNum--
				if (retryCount < AdConfig.adload_retry_num) {
					android.os.Handler(context.mainLooper).postDelayed({
						if (!isLoaded) {
							Log.e(TAG, "loadMaxInter fail, retrying...")
							if (retryCount < AdConfig.adload_retry_num) {
								Log.e(TAG, "loadMaxInter: 广告加载超时重试")
								loadMaxInter(context, retryCount + 1)
							} else {
								Log.e(TAG, "loadMaxInter reached max retry count on fail.")
							}
						}
					}, 1000)
				} else {
					Log.e(TAG, "loadMaxInter reached max retry count.")
				}
			}
			override fun onAdDisplayed(maxAd: MaxAd) { }
			override fun onAdHidden(maxAd: MaxAd) { }
			override fun onAdClicked(maxAd: MaxAd) { }
			override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) { }
		})
		interstitialAd.loadAd()
		if (retryCount == 0) {
			android.os.Handler(context.mainLooper).postDelayed({
				if (!isLoaded) {
					Log.e(TAG, "loadMaxInter timeout, retrying...")
					if (retryCount < AdConfig.adload_retry_num) {
						Log.e(TAG, "loadMaxInter: loadMaxInter 广告加载超时重试")
						loadMaxInter(context, retryCount + 1)
					} else {
						Log.e(TAG, "loadMaxInter reached max retry count on timeout.")
					}
				}
			}, AdConfig.adload_max_time)
		}
	}

	private fun loadTopOnInter(context: Context, retryCount: Int = 0) {
		if (AdPool.topOnInterPool.size + AdPool.toponInterIsLoadingNum >= AdConfig.adload_poolsize_open) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.toponInterIsLoadingNum++
		val mInterstitialAd = TUInterstitial(context, AdvIDs.TopON_INTERSTITIAL_ID)
		// 添加监听
		mInterstitialAd.setAdListener(object : TUInterstitialListener{
			override fun onInterstitialAdLoaded() {
				isLoaded = true
				AdPool.admobInterIsLoadingNum--
				Log.e(TAG, "预加载广告成功 loadTopOnInter Ad was loaded.")
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to "PreLoad",
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (mInterstitialAd.checkAdStatus().tuTopAdInfo.networkName ?: "unknow"),
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						LogAdParam.ad_preload to true,
					)
				)
				AdPool.putTopOnInter(mInterstitialAd)
			}

			override fun onInterstitialAdLoadFail(p0: AdError?) {
				Log.e(TAG, "预加载广告失败 $p0.message")
				AdPool.toponInterIsLoadingNum--
				if (retryCount < AdConfig.adload_retry_num) {
					android.os.Handler(context.mainLooper).postDelayed({
						if (!isLoaded) {
							Log.e(TAG, "loadTopOnInter fail, retrying...")
							if (retryCount < AdConfig.adload_retry_num) {
								Log.e(TAG, "loadTopOnInter: 广告加载超时重试")
								loadTopOnInter(context, retryCount + 1)
							} else {
								Log.e(TAG, "loadTopOnInter reached max retry count on fail.")
							}
						}
					}, 1000)
				} else {
					Log.e(TAG, "loadTopOnInter reached max retry count.")
				}
			}

			override fun onInterstitialAdClicked(p0: TUAdInfo?) {}
			override fun onInterstitialAdShow(p0: TUAdInfo?) {}
			override fun onInterstitialAdClose(p0: TUAdInfo?) {}
			override fun onInterstitialAdVideoStart(p0: TUAdInfo?) {}
			override fun onInterstitialAdVideoEnd(p0: TUAdInfo?) {}
			override fun onInterstitialAdVideoError(p0: AdError?) {}
		})
		mInterstitialAd.load()
		if (retryCount == 0) {
			android.os.Handler(context.mainLooper).postDelayed({
				if (!isLoaded) {
					Log.e(TAG, "loadTopOnInter timeout, retrying...")
					if (retryCount < AdConfig.adload_retry_num) {
						Log.e(TAG, "loadTopOnInter: loadTopOnInter 广告加载超时重试")
						loadTopOnInter(context, retryCount + 1)
					} else {
						Log.e(TAG, "loadTopOnInter reached max retry count on timeout.")
					}
				}
			}, AdConfig.adload_max_time)
		}
	}

	private lateinit var tuAd: TUSplashAd
	private fun loadTopOnOpen(context: Context, retryCount: Int = 0) {
		if (AdPool.topOnOpenPool.size + AdPool.toponOpenIsLoadingNum >= AdConfig.adload_poolsize_open) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.toponOpenIsLoadingNum++
		tuAd = TUSplashAd(context, AdvIDs.TopON_OPEN_ID, object : TUSplashAdListener{
			override fun onAdLoaded(p0: Boolean) {
				Log.e(TAG, "预加载广告成功 loadTopOnOpen Ad was loaded.")
				isLoaded = true
				AdPool.toponOpenIsLoadingNum--
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to "PreLoad",
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
						LogAdParam.ad_preload to true,
					)
				)
				AdPool.putTopOnOpen(tuAd)
			}

			override fun onAdLoadTimeout() {
				Log.e(TAG, "预加载广告失败 onAdLoadTimeout")
				AdPool.toponOpenIsLoadingNum--
				if (retryCount < AdConfig.adload_retry_num) {
					android.os.Handler(context.mainLooper).postDelayed({
						if (!isLoaded) {
							Log.e(TAG, "loadTopOnOpen fail, retrying...")
							if (retryCount < AdConfig.adload_retry_num) {
								Log.e(TAG, "loadTopOnOpen: 广告加载超时重试")
								loadTopOnOpen(context, retryCount + 1)
							} else {
								Log.e(TAG, "loadTopOnOpen reached max retry count on fail.")
							}
						}
					}, 1000)
				} else {
					Log.e(TAG, "loadTopOnOpen reached max retry count.")
				}
			}

			override fun onNoAdError(p0: AdError?) {
				Log.e(TAG, "预加载广告失败 onNoAdError: $p0")
				AdPool.toponOpenIsLoadingNum--
			}

			override fun onAdShow(p0: TUAdInfo?) {}
			override fun onAdClick(p0: TUAdInfo?) {}
			override fun onAdDismiss(
				p0: TUAdInfo?,
				p1: TUSplashAdExtraInfo?
			) {}
		})
		tuAd.loadAd()
		if (retryCount == 0) {
			android.os.Handler(context.mainLooper).postDelayed({
				if (!isLoaded) {
					Log.e(TAG, "loadTopOnOpen timeout, retrying...")
					if (retryCount < AdConfig.adload_retry_num) {
						Log.e(TAG, "loadTopOnOpen: loadTopOnOpen 广告加载超时重试")
						loadTopOnOpen(context, retryCount + 1)
					} else {
						Log.e(TAG, "loadTopOnOpen reached max retry count on timeout.")
					}
				}
			}, AdConfig.adload_max_time)
		}
	}
}