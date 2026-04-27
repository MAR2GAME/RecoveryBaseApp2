package com.quickrecover.photonvideotool.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickrecover.photonvideotool.core.bean.FileData
import com.quickrecover.photonvideotool.core.bean.FoldBean
import com.quickrecover.photonvideotool.core.bean.FoldSortBean
import com.quickrecover.photonvideotool.core.model.RecoveryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class RecoveryViewModel(
	private val recoveryRepository: RecoveryRepository
): ViewModel(), KoinComponent  {
	val _recoveryFolds = MutableStateFlow(emptyList<FoldBean>())
	val recoveryFolds: StateFlow<List<FoldBean>> = _recoveryFolds

	private val _currentFold = MutableStateFlow(FoldBean())
	val currentFold: StateFlow<FoldBean> = _currentFold
	private val _currentScanType = MutableStateFlow(String())
	val currentScanType: StateFlow<String> = _currentScanType

	val _foldSortBeans = MutableStateFlow(emptyList<FoldSortBean>())
	val foldSortBeans: StateFlow<List<FoldSortBean>> = _foldSortBeans
	private val _recoveredFiles = MutableStateFlow(emptyList<FileData>())
	val recoveredFiles: StateFlow<List<FileData>> = _recoveredFiles

	// 0 - All Days, 1 - Within 7 Days, 2 - Within 1 month, 3 - Within 6 month
	val _foldFilter = MutableStateFlow(0)
	val foldFilter: StateFlow<Int> = _foldFilter
	// 底部弹框用的临时变量，默认和foldFilter相同
	val _tmpFoldFilter = MutableStateFlow(_foldFilter.value)
	val tmpFoldFilter: StateFlow<Int> = _tmpFoldFilter

	// 0 - Date, 1 - File Size
	val _sortBy = MutableStateFlow(0)
	val sortBy: StateFlow<Int> = _sortBy
	val _tmpSortBy = MutableStateFlow(_sortBy.value)
	val tmpSortBy: StateFlow<Int> = _tmpSortBy

	// 0 - Asc, 1 - Desc
	val _orderType = MutableStateFlow(1)
	val orderType: StateFlow<Int> = _orderType
	// 底部弹框用的临时变量，默认和orderType相同
	val _tmpOrderType = MutableStateFlow(_orderType.value)
	val tmpOrderType: StateFlow<Int> = _tmpOrderType

	private val _hideLowQualityPhotos = MutableStateFlow(false)
	val hideLowQualityPhotos: StateFlow<Boolean> = _hideLowQualityPhotos
	val _hideLowQualityPhotosSize = MutableStateFlow(0)
	val hideLowQualityPhotosSize: StateFlow<Int> = _hideLowQualityPhotosSize

	private val _isSelectAll = MutableStateFlow(false)
	val isSelectAll: StateFlow<Boolean> = _isSelectAll

	private val _hasRecovery = MutableStateFlow(false)
	val hasRecovery: StateFlow<Boolean> = _hasRecovery

	private val _tips = MutableStateFlow("")
	val tips: StateFlow<String> = _tips
	private val _tipsTime = MutableStateFlow(0L)
	val tipsTime: StateFlow<Long> = _tipsTime

	fun selectAll(isSelect: Boolean) {
		_isSelectAll.value = isSelect
		// 选中 _currentFold 中的数据
		viewModelScope.launch(Dispatchers.Default) {
			_currentFold.value = _currentFold.value.copy(
				files = _currentFold.value.files.map { file ->
					file.copy(isSelect = isSelect)
				}
			)
		}
		// 所有的FoldSortBean全选
		viewModelScope.launch(Dispatchers.Default) {
			_foldSortBeans.value = _foldSortBeans.value.map { foldSortBean ->
				foldSortBean.copy(
					files = foldSortBean.files.map { file ->
						file.copy(isSelect = isSelect)
					},
					checked = isSelect
				)
			}
		}
	}

	fun selectSort(isSelect: Boolean, foldSortBean: FoldSortBean) {
		// 选中 _currentFold 中的数据
		viewModelScope.launch(Dispatchers.Default) {
			_currentFold.value = _currentFold.value.copy(
				files = _currentFold.value.files.map { file ->
					val foldSortName = run {
						val calendar = java.util.Calendar.getInstance()
						calendar.timeInMillis = file.lastModified
						val year = calendar.get(java.util.Calendar.YEAR)
						val month = calendar.get(java.util.Calendar.MONTH) + 1
						val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
						String.format("%04d-%02d-%02d", year, month, day)
					}
					if (foldSortName == foldSortBean.name) {
						file.copy(isSelect = isSelect)
					} else {
						file
					}
				}
			)
		}
		// 选中FoldSortBean
		viewModelScope.launch(Dispatchers.Default) {
			_foldSortBeans.value = _foldSortBeans.value.map { bean ->
				if (bean.name == foldSortBean.name) {
					bean.copy(checked = isSelect, files = bean.files.map { file ->
						file.copy(isSelect = isSelect)
					})
				} else {
					bean
				}
			}
		}
	}

	fun selectFile(isSelect: Boolean, fileData: FileData) {
		// 选中FileData
		viewModelScope.launch(Dispatchers.Default) {
			// 选中 _currentFold 中的数据
			_currentFold.value = _currentFold.value.copy(
				files = _currentFold.value.files.map { file ->
					if (file.path == fileData.path) {
						file.copy(isSelect = isSelect)
					} else {
						file
					}
				}
			)
			// 选中 _foldSortBeans 中的数据
			_foldSortBeans.value = _foldSortBeans.value.map { foldSortBean ->
				foldSortBean.copy(
					files = foldSortBean.files.map { file ->
						if (file.path == fileData.path) {
							file.copy(isSelect = isSelect)
						} else {
							file
						}
					}
				)
			}
		}
	}

	fun getRecoveryFiles(scanType: String) {
		viewModelScope.launch(Dispatchers.IO) {
			_recoveryFolds.value = recoveryRepository.getRecoveryFiles(scanType)
		}
	}

	fun setCurrenntFold(scanType: String, foldBean: FoldBean) {
		_currentScanType.value = scanType
		_currentFold.value = foldBean
		getFoldSortBeans()
		viewModelScope.launch(Dispatchers.IO) {
			recoveryRepository.saveCurrentFold(scanType, _currentFold.value)
		}
	}

	fun setTmpFilter() {
		_tmpFoldFilter.value = _foldFilter.value
	}

	fun setTmpFilter(filter: Int) {
		_tmpFoldFilter.value = filter
	}

	fun setFilter() {
		_foldFilter.value = _tmpFoldFilter.value
		getFoldSortBeans()
	}

	fun resetFilter() {
		_foldFilter.value = 0
		getFoldSortBeans()
	}

	fun setTmpSortAndOrder() {
		_tmpSortBy.value = _sortBy.value
		_tmpOrderType.value = _orderType.value
	}

	fun setTmpSort(sortBy: Int) {
		_tmpSortBy.value = sortBy
	}

	fun setTmpOrder(orderType: Int) {
		_tmpOrderType.value = orderType
	}

	fun setSortAndOrder() {
		_sortBy.value = _tmpSortBy.value
		_orderType.value = _tmpOrderType.value
		getFoldSortBeans()
	}

	fun resetSortAndOrder() {
		_sortBy.value = 0
		_orderType.value = 0
		getFoldSortBeans()
	}

	fun setHideLowQualityPhotos(hide: Boolean) {
		_hideLowQualityPhotos.value = hide
//		getFoldSortBeans()
	}

	fun recoveryFiles(context: Context, scanType: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val filesToRecovery = _currentFold.value.files.filter { it.isSelect }
			if (filesToRecovery.isEmpty()) {
				return@launch
			}
			val recoveredFiles = mutableListOf<FileData>()
			filesToRecovery.forEach { file ->
				val newFile = recoveryRepository.recoveryFile(context, file)
				if (newFile.name.isNotEmpty()) {
					recoveredFiles.add(newFile)
				}
			}
			_recoveredFiles.value = recoveredFiles
			saveRecoveredFiles()
			val remainingFiles = _currentFold.value.files.filter { !it.isSelect }
			val newFoldSortBeans = _foldSortBeans.value
				.map { foldSortBean ->
					val remainingFilesInBean = foldSortBean.files.filter { file ->
						!filesToRecovery.any { it.path == file.path }
					}
					foldSortBean.copy(files = remainingFilesInBean)
				}
				.filter { it.files.isNotEmpty() }
			withContext(Dispatchers.Main) {
				_currentFold.value = _currentFold.value.copy(files = remainingFiles)
				_foldSortBeans.value = newFoldSortBeans
				_hasRecovery.value = true
			}
			updateFold(scanType, filesToRecovery)
		}
	}

	fun recoveryFile(context: Context, scanType: String, filePath: String) {
		viewModelScope.launch(Dispatchers.IO) {
			Log.e("TAG", "recoveryFile: ${_currentFold.value.files.size}", )
			val filesToRecovery = _currentFold.value.files.filter { it.path == filePath }
			if (filesToRecovery.isEmpty()) {
				return@launch
			}
			val recoveredFiles = mutableListOf<FileData>()
			filesToRecovery.forEach { file ->
				val newFile = recoveryRepository.recoveryFile(context, file)
				if (newFile.name.isNotEmpty()) {
					recoveredFiles.add(newFile)
				}
			}
			_recoveredFiles.value = recoveredFiles
			saveRecoveredFiles()
			val remainingFiles = _currentFold.value.files.filter { it.path != filePath }
			val newFoldSortBeans = _foldSortBeans.value
				.map { foldSortBean ->
					val remainingFilesInBean = foldSortBean.files.filter { file ->
						!filesToRecovery.any { it.path == file.path }
					}
					foldSortBean.copy(files = remainingFilesInBean)
				}
				.filter { it.files.isNotEmpty() }
			withContext(Dispatchers.Main) {
				_currentFold.value = _currentFold.value.copy(files = remainingFiles)
				_foldSortBeans.value = newFoldSortBeans
				_hasRecovery.value = true
			}
			updateFold(scanType, filesToRecovery)
		}
	}

	fun delFiles(scanType: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val filesToDelete = _currentFold.value.files.filter { it.isSelect }
			filesToDelete.forEach { file ->
				recoveryRepository.delFile(file)
			}
			val remainingFiles = _currentFold.value.files.filter { !it.isSelect }
			val newFoldSortBeans = _foldSortBeans.value
				.map { foldSortBean ->
					val remainingFilesInBean = foldSortBean.files.filter { file ->
						!filesToDelete.any { it.path == file.path }
					}
					foldSortBean.copy(files = remainingFilesInBean)
				}
				.filter { it.files.isNotEmpty() }
			withContext(Dispatchers.Main) {
				_currentFold.value = _currentFold.value.copy(files = remainingFiles)
				_foldSortBeans.value = newFoldSortBeans
				if (filesToDelete.isNullOrEmpty()) {
					_tips.value = "Please select one item"
				} else {
					_tips.value = "Delete files success"
				}
				_tipsTime.value = System.currentTimeMillis()
			}
			updateFold(scanType, filesToDelete)
		}
	}

	fun delFile(scanType: String, filePath: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val filesToDelete = _currentFold.value.files.filter { it.path == filePath }
			filesToDelete.forEach { file ->
				recoveryRepository.delFile(file)
			}
			val remainingFiles = _currentFold.value.files.filter { it.path != filePath }
			val newFoldSortBeans = _foldSortBeans.value
				.map { foldSortBean ->
					val remainingFilesInBean = foldSortBean.files.filter { file ->
						!filesToDelete.any { it.path == file.path }
					}
					foldSortBean.copy(files = remainingFilesInBean)
				}
				.filter { it.files.isNotEmpty() }
			withContext(Dispatchers.Main) {
				_currentFold.value = _currentFold.value.copy(files = remainingFiles)
				_foldSortBeans.value = newFoldSortBeans
				if (filesToDelete.isNullOrEmpty()) {
					_tips.value = "Please select one item"
				} else {
					_tips.value = "Delete files success"
				}
				_tipsTime.value = System.currentTimeMillis()
			}
			updateFold(scanType, filesToDelete)
		}
	}

	fun resetHasRecovery() {
		_hasRecovery.value = false
	}

	suspend fun updateFold(scanType: String, delFileList: List<FileData>) {
		val fold = _currentFold.value.copy(
			files = _currentFold.value.files.map { file ->
				if (delFileList.any { it.path == file.path }) {
					file.copy(isSelect = false)
				} else {
					file
				}
			}
		)
		_currentFold.value = fold
		Log.e("TAG", "updateFold: ${fold.files.size}" )
		recoveryRepository.saveCurrentFold(scanType, fold)

		val allData = recoveryRepository.getScanResult(scanType)
		val filteredData = allData.filterNot { file ->
			delFileList.any { it.path == file.path }
		}
		recoveryRepository.saveScanResult(scanType, filteredData)
	}

	suspend fun getCurrentFold(scanType: String = "recovery_photos"): FoldBean {
		_currentScanType.value = scanType
		_currentFold.value = recoveryRepository.getCurrentFold(scanType)
		return _currentFold.value
	}

	fun saveRecoveredFiles() {
		viewModelScope.launch(Dispatchers.IO) {
			recoveryRepository.saveRecoveredFiles(scanType = _currentScanType.value, _recoveredFiles.value)
		}
	}

	fun clearCurrentFold(scanType: String = "recovery_photos") {
		viewModelScope.launch(Dispatchers.IO) {
			recoveryRepository.clearCurrentFold(scanType)
		}
	}

	fun getFoldSortBeans() {
		val now = System.currentTimeMillis()

		val lowQualityCount = _currentFold.value.files.count { it.size < 1_048_576 }
		_hideLowQualityPhotosSize.value = lowQualityCount

		val filteredFiles = _currentFold.value.files.filter { file ->
			val passFilter = when (_foldFilter.value) {
				0 -> true
				1 -> now - file.lastModified <= 7L * 24 * 60 * 60 * 1000
				2 -> now - file.lastModified <= 30L * 24 * 60 * 60 * 1000
				3 -> now - file.lastModified <= 180L * 24 * 60 * 60 * 1000
				else -> true
			}
			val passQuality = if (_hideLowQualityPhotos.value) file.size >= 1_048_576 else true // 1MB = 1024*1024=1048576 bytes
			passFilter && passQuality
		}

		val sortedFiles = when (_sortBy.value) {
			0 -> if (_orderType.value == 0) filteredFiles.sortedBy { it.lastModified } else filteredFiles.sortedByDescending { it.lastModified }
			1 -> if (_orderType.value == 0) filteredFiles.sortedBy { it.size } else filteredFiles.sortedByDescending { it.size }
			else -> filteredFiles
		}
		if (_sortBy.value == 1) {
			_foldSortBeans.value = listOf(
				FoldSortBean(
					name = _currentFold.value.name,
					files = sortedFiles,
					checked = false
				)
			)
		} else {
			// 根据 lastModified 日期分组，FoldSortBean.name 为日期字符串
			val grouped = sortedFiles.groupBy { file ->
				val calendar = java.util.Calendar.getInstance()
				calendar.timeInMillis = file.lastModified
				val year = calendar.get(java.util.Calendar.YEAR)
				val month = calendar.get(java.util.Calendar.MONTH) + 1 // 月份从0开始
				val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
				// 格式化日期为 "YYYY-MM-DD"
				String.format("%04d-%02d-%02d", year, month, day)
			}

			val foldSortList = if (_orderType.value == 0) {
				grouped.entries.sortedBy { it.key }.map { entry ->
					FoldSortBean(
						name = entry.key,
						files = entry.value,
						checked = false
					)
				}
			} else {
				grouped.entries.sortedByDescending { it.key }.map { entry ->
					FoldSortBean(
						name = entry.key,
						files = entry.value,
						checked = false
					)
				}
			}

			_foldSortBeans.value = foldSortList
		}
	}
}