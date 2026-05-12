package com.datatool.photorecovery

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.datatool.photorecovery.core.advertise.AdvertiseConfigFactory
import com.datatool.photorecovery.core.di.allModules
import com.datatool.photorecovery.core.model.RecoveryRepository
import com.pdffox.adv.AdvertiseSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
class MyApp: Application(), DefaultLifecycleObserver {

	companion object {
		private const val TAG = "MyApp"
		lateinit var instance: MyApp
			private set
	}
	private val recoveryRepository: RecoveryRepository by inject()

	override fun onCreate() {
		super<Application>.onCreate()
		startKoin {
			androidContext(this@MyApp)
			modules(allModules)
		}
		instance = this
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
		kotlinx.coroutines.runBlocking {
			AdvertiseSdk.init(this@MyApp, BuildConfig.DEBUG, AdvertiseConfigFactory.create(this@MyApp))
		}
	}

	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		CoroutineScope(Dispatchers.IO).launch {
			recoveryRepository.clearCapacityItems()
		}
	}
}
