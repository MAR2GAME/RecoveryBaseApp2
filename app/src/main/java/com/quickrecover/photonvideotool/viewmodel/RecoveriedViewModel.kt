package com.quickrecover.photonvideotool.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickrecover.photonvideotool.core.bean.FileData
import com.quickrecover.photonvideotool.core.model.RecoveryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class RecoveriedViewModel(
	private val recoveryRepository: RecoveryRepository
): ViewModel(), KoinComponent  {
	private val _currentScanType = MutableStateFlow(String())
	val currentScanType: StateFlow<String> = _currentScanType

	private val _recoveredFiles = MutableStateFlow(emptyList<FileData>())
	val recoveredFiles: StateFlow<List<FileData>> = _recoveredFiles

	fun setCurrentScanType(scanType: String) {
		Log.e("TAG", "setCurrentScanType: $scanType", )
		viewModelScope.launch(Dispatchers.Main) {
			_currentScanType.value = scanType
			getRecoveredFiles()
			clearRecoveredFilesStatus()
		}
	}

	fun delRecoveredFiles() {
		viewModelScope.launch(Dispatchers.Default) {
			recoveryRepository.delRecoveredFiles(_currentScanType.value, _recoveredFiles.value.filter { it.isSelect })
			_recoveredFiles.value = recoveryRepository.getRecoveredFiles(_currentScanType.value).distinctBy { it.path }
		}
	}

	fun clearRecoveredFilesStatus() {
		_recoveredFiles.value = _recoveredFiles.value.map {
			it.copy(isSelect = false)
		}
	}

	fun setFileSelect(isSelect: Boolean, fileData: FileData) {
		_recoveredFiles.value = _recoveredFiles.value.map {
			if (it == fileData) {
				it.copy(isSelect = isSelect)
			} else {
				it
			}
		}
	}

	suspend fun getRecoveredFiles() {
		_recoveredFiles.value = recoveryRepository
			.getRecoveredFiles(_currentScanType.value).distinctBy { it.path }
		Log.e("TAG", "getRecoveredFiles: ${_recoveredFiles.value.size}" )
	}

}