package com.quickrecover.photonvideotool

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.quickrecover.photonvideotool.core.di.allModules
import com.quickrecover.photonvideotool.core.model.RecoveryRepository
import com.mar2game.safe.SafeChecker
import com.quickrecover.photonvideotool.util.PreferenceUtil
/*import com.pdffox.adv.use.Ads
import com.pdffox.adv.use.AdvApplicaiton*/
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// TODO: change
/*class MyApp: AdvApplicaiton(), DefaultLifecycleObserver {

	companion object {
		private const val TAG = "MyApp"
		lateinit var instance: MyApp
			private set
	}
	private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val recoveryRepository: RecoveryRepository by inject()

	override fun onCreate() {
		super<AdvApplicaiton>.onCreate()
		startKoin {
			androidContext(this@MyApp)
			modules(allModules)
		}
		instance = this
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
		kotlinx.coroutines.runBlocking {
			Ads.init(this@MyApp, BuildConfig.DEBUG)
		}
		applicationScope.launch {
			delay(1000) // 延迟1秒，让应用先启动
			SafeChecker.checkAndShutDown(this@MyApp)
		}

	}

	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		CoroutineScope(Dispatchers.IO).launch {
			recoveryRepository.clearCapacityItems()
		}
	}
}*/

class MyApp: Application(), DefaultLifecycleObserver {

	companion object {
		private const val TAG = "MyApp"
		lateinit var instance: MyApp
			private set
	}
	private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val recoveryRepository: RecoveryRepository by inject()

	override fun onCreate() {
		super<Application>.onCreate()
		startKoin {
			androidContext(this@MyApp)
			modules(allModules)
		}
		instance = this
		PreferenceUtil.init(this)
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//		kotlinx.coroutines.runBlocking {
//			Ads.init(this@MyApp, BuildConfig.DEBUG)
//		}
		applicationScope.launch {
			delay(1000) // 延迟1秒，让应用先启动
			SafeChecker.checkAndShutDown(this@MyApp)
		}

	}

	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		CoroutineScope(Dispatchers.IO).launch {
			recoveryRepository.clearCapacityItems()
		}
	}
}