package com.pdffox.adv.remoteconfig

import android.app.Activity
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.pdffox.adv.Ads
import com.pdffox.adv.adv.AdConfig

object RemoteConfigManager {
	private const val TAG = "RemoteConfigManager"
	@Volatile
	private var initialized = false

	fun initRemoteConfig() {
		if (initialized) {
			return
		}
		initialized = true
		val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
		val configSettings = remoteConfigSettings {
			minimumFetchIntervalInSeconds = 3600
		}
		remoteConfig.setConfigSettingsAsync(configSettings)
		remoteConfig.fetchAndActivate()
			.addOnCompleteListener{ task ->
				if (task.isSuccessful) {
					RemoteConfig.update(remoteConfig)
				} else {
					Log.e(TAG, "onComplete: ", task.exception)
				}
			}
		remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
			override fun onUpdate(configUpdate : ConfigUpdate) {
				RemoteConfig.update(remoteConfig)
			}
			override fun onError(error : FirebaseRemoteConfigException) {
				Log.e(TAG, "onError: ", error)
			}
		})
	}

}
