package com.quickrecover.photonvideotool.viewmodel

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickrecover.photonvideotool.core.bean.AllCapacityInfo
import com.quickrecover.photonvideotool.core.bean.CapacityBean
import com.quickrecover.photonvideotool.core.model.PermissionRepository
import com.quickrecover.photonvideotool.core.model.RecoveryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class HomeViewModel(
	private val permissionRepository: PermissionRepository,
	private val recoveryRepository: RecoveryRepository
): ViewModel(), KoinComponent {

	// 通知栏权限
	private val _hasNotificationPri = MutableStateFlow(false)
	val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPri
	private val _hasFileManagerPri = MutableStateFlow(false)
	val hasFileManagerPermission: StateFlow<Boolean> = _hasFileManagerPri

	// 所有存储容量信息
	private val _allCapacityInfo = MutableStateFlow(AllCapacityInfo(0, 0))
	val allCapacityInfo: StateFlow<AllCapacityInfo> = _allCapacityInfo

	private val _capacityItems = MutableStateFlow(emptyList<CapacityBean>())
	val capacityItems: StateFlow<List<CapacityBean>> = _capacityItems

	private val _capacityDetailItems = MutableStateFlow(emptyList<CapacityBean>())
	val capacityDetailItems: StateFlow<List<CapacityBean>> = _capacityDetailItems

	fun getAllCapacityInfo() {
		_allCapacityInfo.value = recoveryRepository.getAllCapacityInfo()
	}

	fun checkNotificationPermission() {
		_hasNotificationPri.value = permissionRepository.checkNotificationPermission()
	}

	fun requestNotificationPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
		permissionRepository.requestNotificationPermission(requestPermissionLauncher)
	}

	fun getCapacityItems() {
		viewModelScope.launch(Dispatchers.IO) {
			if (!permissionRepository.checkFileManagerPermission()) {
				_capacityItems.value = emptyList()
				return@launch
			}
			val items = recoveryRepository.getCapacityItems().filter {
				it.name == "Photos"
				it.name == "Videos"
			}
			_capacityItems.value = items
		}
	}

	fun getCapacityDetailItems() {
		viewModelScope.launch(Dispatchers.IO) {
			if (!permissionRepository.checkFileManagerPermission()) {
				_capacityDetailItems.value = emptyList()
				return@launch
			}
			val items = recoveryRepository.getCapacityItems()
			_capacityDetailItems.value = items
		}
	}

	fun checkFileManagerPermission() {
		_hasFileManagerPri.value = permissionRepository.checkFileManagerPermission()
	}

	fun requestFileManagerPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
		permissionRepository.requestFileManagerPermission(requestPermissionLauncher)
	}
}