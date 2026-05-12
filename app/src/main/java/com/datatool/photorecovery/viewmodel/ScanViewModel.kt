package com.datatool.photorecovery.viewmodel

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datatool.photorecovery.core.bean.FileData
import com.datatool.photorecovery.core.model.PermissionRepository
import com.datatool.photorecovery.core.model.RecoveryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class ScanViewModel(
	private val permissionRepository: PermissionRepository,
	private val recoveryRepository: RecoveryRepository
): ViewModel(), KoinComponent {
	// 文件管理权限
	private var _hasCheckedFileManagerPermission = MutableStateFlow(false)
	val hasCheckedFileManagerPermission: StateFlow<Boolean> = _hasCheckedFileManagerPermission

	private val _hasFileManagerPri = MutableStateFlow(false)
	val hasFileManagerPermission: StateFlow<Boolean> = _hasFileManagerPri

	// 当前正在扫描的文件路径
	private val _currentScanningPath = MutableStateFlow("")
	val currentScanningPath: StateFlow<String> = _currentScanningPath

	val _scanFilesResult = MutableStateFlow(emptyList<FileData>())
	val scanFilesResult: StateFlow<List<FileData>> = _scanFilesResult
	val _scanFilesEmpty = MutableStateFlow(false)
	val scanFilesEmpty: StateFlow<Boolean> = _scanFilesEmpty

	fun checkFileManagerPermission() {
		_hasFileManagerPri.value = permissionRepository.checkFileManagerPermission()
		_hasCheckedFileManagerPermission.value = true
	}

	fun requestFileManagerPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
		permissionRepository.requestFileManagerPermission(requestPermissionLauncher)
	}

	fun scanFiles(context: Context, scanType: String = "recovery_photos") {
		Log.e("TAG", "scanFiles: $scanType")
		viewModelScope.launch(Dispatchers.IO) {
			val result = recoveryRepository.scanFiles(context, scanType) { filePath ->
				viewModelScope.launch(Dispatchers.Main) {
					_currentScanningPath.value = filePath
				}
			}
			Log.e("TAG", "scanFiles: $result", )
			recoveryRepository.saveScanResult(scanType, result)
			withContext(Dispatchers.Main) {
				_scanFilesResult.value = result
				_scanFilesEmpty.value = result.isEmpty()
			}
		}
	}

	fun getScanResult(scanType: String = "recovery_photos") {
		viewModelScope.launch(Dispatchers.IO) {
			val result = recoveryRepository.getScanResult(scanType)
			withContext(Dispatchers.Main) {
				_scanFilesResult.value = result
			}
		}
	}

}