package com.quickrecover.photonvideotool.viewmodel

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import com.quickrecover.photonvideotool.BuildConfig
import com.quickrecover.photonvideotool.core.model.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO: add
//import com.pdffox.adv.use.adv.AdConfig

class SplashViewModel(
	private val permissionRepository: PermissionRepository,
) : ViewModel(), KoinComponent {

	private val dataStore: DataStore<Preferences> by inject()

	private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
	private val IS_FIRST_SHOW = booleanPreferencesKey("is_first_show")

	private val _navigateToLanguageSetting = MutableStateFlow(false)
	val navigateToLanguageSetting: StateFlow<Boolean> = _navigateToLanguageSetting

	private val _navigateToGuide = MutableStateFlow(false)
	val navigateToGuide: StateFlow<Boolean> = _navigateToGuide

	private val _navigateToHome = MutableStateFlow(false)
	val navigateToHome: StateFlow<Boolean> = _navigateToHome

	private val _isFirstShow = MutableStateFlow(false)
	val isFirstShow: StateFlow<Boolean> = _isFirstShow

	suspend fun checkFirstLaunch() {
		val prefs = dataStore.data.first()
		val isFirstLaunch = prefs[IS_FIRST_LAUNCH] ?: true
		if (BuildConfig.DEBUG) {
			Log.e("TAG", "checkFirstLaunch: isFirstLaunch = $isFirstLaunch")
		}
		// TODO: change
//		if (isFirstLaunch) {
//			if (AdConfig.ignoreGuide) {
//				_navigateToHome.value = true
//			} else {
//				if (!AdConfig.hasOpenLaungPage) {
//					_navigateToGuide.value = true
//				} else {
//					_navigateToLanguageSetting.value = true
//				}
//			}
//		} else {
			_navigateToHome.value = true
//		}
	}

	suspend fun setFirstLauncher() {
		if (BuildConfig.DEBUG) {
			Log.e("TAG", "setFirstShow: isFirstLaunch = false")
		}
		dataStore.edit { settings ->
			settings[IS_FIRST_LAUNCH] = false
		}
	}

	suspend fun checkFirstShow() {
		val prefs = dataStore.data.first()
		val isFirstShow = prefs[IS_FIRST_SHOW] ?: true
		_isFirstShow.value = isFirstShow
	}

	suspend fun setFirstShow() {
		if (BuildConfig.DEBUG) {
			Log.e("TAG", "setFirstShow: isFirstShow = false")
		}
		dataStore.edit { settings ->
			settings[IS_FIRST_SHOW] = false
		}
	}

	fun requestNotificationPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
		permissionRepository.requestNotificationPermission(requestPermissionLauncher)
	}
}