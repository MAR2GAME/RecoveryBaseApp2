package com.quickrecover.photonvideotool.core.model

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quickrecover.photonvideotool.BuildConfig
import com.quickrecover.photonvideotool.core.bean.AllCapacityInfo
import com.quickrecover.photonvideotool.core.bean.CapacityBean
import com.quickrecover.photonvideotool.core.bean.FileData
import com.quickrecover.photonvideotool.core.bean.FoldBean
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.File

class RecoveryRepository(
	private val dataStore: DataStore<Preferences>
) {	fun getAllCapacityInfo(): AllCapacityInfo {
		val path = Environment.getDataDirectory() // 获取内部存储路径
		val stat = StatFs(path.path)

		val blockSize = stat.blockSizeLong
		val totalBlocks = stat.blockCountLong
		val availableBlocks = stat.availableBlocksLong

		val totalSize = blockSize * totalBlocks
		val availableSize = blockSize * availableBlocks
		val usedSize = totalSize - availableSize
		return AllCapacityInfo(
			totalSize = totalSize,
			usedSize = usedSize
		)
	}

	data class FolderData(
		val size: Long,
		val fileCount: Int
	)

	private fun getFolderData(folder: File?): FolderData {
		if (folder == null || !folder.exists()) return FolderData(0L, 0)
		var size = 0L
		var count = 0
		folder.listFiles()?.forEach { file ->
			if (file.isDirectory) {
				val data = getFolderData(file)
				size += data.size
				count += data.fileCount
			} else {
				size += file.length()
				count += 1
			}
		}
		return FolderData(size, count)
	}

	data class MediaData(
		val photoSize: Long,
		val photoCount: Int,
		val videoSize: Long,
		val videoCount: Int
	)

	private fun getPhotosAndVideosData(folder: File?): MediaData {
		if (folder == null || !folder.exists()) return MediaData(0L, 0, 0L, 0)
		var photoSize = 0L
		var photoCount = 0
		var videoSize = 0L
		var videoCount = 0

		folder.listFiles()?.forEach { file ->
			if (file.isDirectory) {
				val data = getPhotosAndVideosData(file)
				photoSize += data.photoSize
				photoCount += data.photoCount
				videoSize += data.videoSize
				videoCount += data.videoCount
			} else {
				val name = file.name.lowercase()
				when {
					name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp") -> {
						photoSize += file.length()
						photoCount += 1
					}
					name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv") || name.endsWith(".mov") || name.endsWith(".wmv") -> {
						videoSize += file.length()
						videoCount += 1
					}
				}
			}
		}
		return MediaData(photoSize, photoCount, videoSize, videoCount)
	}

	suspend fun getCapacityItems(): List<CapacityBean> {
		val KEY_CAPACITY_ITEMS = stringPreferencesKey("capacity_items")
		val capacityItems = dataStore.data.map { preferences ->
			val jsonString = preferences[KEY_CAPACITY_ITEMS]
			if (jsonString.isNullOrBlank()) {
				emptyList()
			} else {
				try {
					Json.decodeFromString<List<CapacityBean>>(jsonString)
				} catch (e: Exception) {
					emptyList()
				}
			}
		}.firstOrNull() ?: emptyList()

		if (capacityItems.isNotEmpty()) return capacityItems

		val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
		val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
		val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
		val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
		val download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
		val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

		val dcimMediaData = getPhotosAndVideosData(dcim)
		val picturesData = getFolderData(pictures)
		val moviesData = getFolderData(movies)
		val musicData = getFolderData(music)
		val downloadData = getFolderData(download)
		val documentsData = getFolderData(documents)

		val resultList = listOf(
			CapacityBean("Photos", dcimMediaData.photoSize + picturesData.size, dcimMediaData.photoCount + picturesData.fileCount),
			CapacityBean("Videos", dcimMediaData.videoSize + moviesData.size, dcimMediaData.videoCount + moviesData.fileCount),
			CapacityBean("Audios", musicData.size, musicData.fileCount),
			CapacityBean("Downloads", downloadData.size, downloadData.fileCount),
			CapacityBean("Document", documentsData.size, documentsData.fileCount),
		)

		dataStore.edit { preferences ->
			preferences[KEY_CAPACITY_ITEMS] = Json.encodeToString(resultList)
		}

		return resultList
	}

	suspend fun clearCapacityItems() {
		val KEY_CAPACITY_ITEMS = stringPreferencesKey("capacity_items")
		dataStore.edit { preferences ->
			preferences[KEY_CAPACITY_ITEMS] = ""
		}
	}

	fun scanFiles(context: Context, scanType: String = "recovery_photos" ,callback: (String) -> Unit): List<FileData> {
		val fileList = mutableListOf<FileData>()
		when(scanType){
			"recovery_photos" -> {
				val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				traverseAndCallback(context, scanType, pictures, fileList, callback)
				val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
				traverseAndCallback(context, scanType, dcim, fileList, callback)
			}
			"recovery_videos" -> {
				val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
				traverseAndCallback(context, scanType, movies, fileList, callback)
				val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
				traverseAndCallback(context, scanType, dcim, fileList, callback)
			}
			"recovery_audios" -> {
				val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
				traverseAndCallback(context, scanType, music, fileList, callback)
			}
			else -> {
				val download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				traverseAndCallback(context, scanType, download, fileList, callback)
				val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
				traverseAndCallback(context, scanType, documents, fileList, callback)
			}
		}
		return fileList
	}

//	fun scanFiles(context: Context, scanType: String = "recovery_photos", callback: (String) -> Unit): List<FileData> {
//		val fileList = mutableListOf<FileData>()
//		val rootDir = Environment.getExternalStorageDirectory() // 外部存储根目录
//		traverseAndCallback(context, scanType, rootDir, fileList, callback)
//		return fileList
//	}

	private fun traverseAndCallback(context: Context, scanType: String, folder: File?, fileList: MutableList<FileData> , callback: (String) -> Unit) {
		if (folder == null || !folder.exists()) return
		folder.listFiles()?.forEach { file ->
			if (file.isFile) {
				when(scanType){
					"recovery_photos" -> {
						if(file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || file.name.endsWith(".png") || file.name.endsWith(".gif") || file.name.endsWith(".bmp")){
							callback(file.name)
							// 如果是可恢复索引的文件，添加到列表中
							if(!isFileIndexedInMediaStore(context, file)){
								fileList.add(FileData(file.name, file.absolutePath, file.length(), file.lastModified()))
							}
						}
					}
					"recovery_videos" -> {
						if(file.name.endsWith(".mp4") || file.name.endsWith(".avi") || file.name.endsWith(".mkv") || file.name.endsWith(".mov") || file.name.endsWith(".wmv")){
							callback(file.name)
							// 如果是可恢复索引的文件，添加到列表中
							if(!isFileIndexedInMediaStore(context, file)) {
								fileList.add(FileData(file.name, file.absolutePath, file.length(), file.lastModified()))
							}
						}
					}
					else -> {
						callback(file.name)
						// 如果是可恢复索引的文件，添加到列表中
						if(!isFileIndexedInMediaStore(context, file)) {
							fileList.add(FileData(file.name, file.absolutePath, file.length(), file.lastModified()))
						}
					}
				}
			}
			if (file.isDirectory) {
				if (
					file.path == Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/RecoveredDocs" ||
					file.path == Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/RecoveredAudios" ||
					file.path == Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/RecoveredArchives" ||
					file.path == Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/RecoveredOthers" ||
					file.path == Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Recovered"				) {
				} else {
					traverseAndCallback(context, scanType, file, fileList, callback)
				}
			}
		}
	}

	fun isFileIndexedInMediaStore(context: Context, file: File): Boolean {
		if (BuildConfig.DEBUG) {
			return false
		} else {
			val filePath = file.absolutePath
			val uri = MediaStore.Files.getContentUri("external")
			val projection = arrayOf(MediaStore.MediaColumns.DATA)
			val selection = "${MediaStore.MediaColumns.DATA} = ?"
			val selectionArgs = arrayOf(filePath)

			context.contentResolver.query(uri, projection, selection, selectionArgs, null).use { cursor ->
				return cursor != null && cursor.moveToFirst()
			}
		}
	}

	suspend fun saveScanResult(scanType: String = "recovery_photos", fileList: List<FileData>) {
		val key = stringPreferencesKey("scan_result_$scanType")
		val jsonString = Json.encodeToString(fileList)
		dataStore.edit { prefs ->
			prefs[key] = jsonString
		}
	}

	suspend fun saveCurrentFold(scanType: String = "recovery_photos", currentFold: FoldBean) {
		val key = stringPreferencesKey("current_fold_$scanType")
		val jsonString = Json.encodeToString(currentFold)
		dataStore.edit { prefs ->
			prefs[key] = jsonString
		}
	}

	suspend fun getCurrentFold(scanType: String = "recovery_photos"): FoldBean {
		val key = stringPreferencesKey("current_fold_$scanType")
		return dataStore.data.map { prefs ->
			Json.decodeFromString<FoldBean>(prefs[key] ?: "{}")
		}.firstOrNull() ?: FoldBean()
	}

	suspend fun clearCurrentFold(scanType: String = "recovery_photos") {
		saveCurrentFold(scanType, FoldBean())
	}

	suspend fun getScanResult(scanType: String = "recovery_photos"): List<FileData> {
		val key = stringPreferencesKey("scan_result_$scanType")
		return dataStore.data.map { prefs ->
			Json.decodeFromString<List<FileData>>(prefs[key] ?: "[]")
		}.firstOrNull() ?: emptyList()
	}

	suspend fun getRecoveryFiles(scanType: String = "recovery_photos"): List<FoldBean> {
		val scanResult = getScanResult(scanType)
		val grouped = scanResult.groupBy {
			val parentName = File(it.path).parentFile?.name
			parentName ?: "unknown"
		}.map { (key, value) ->
			FoldBean(key, value)
		}
		return if (scanResult.isNotEmpty()) {
			listOf(FoldBean("All", scanResult)) + grouped
		} else {
			grouped
		}
	}

	fun delFile(file: FileData): Boolean {
		val result = File(file.path).delete()
		if (!result) {
			Log.e("TAG", "删除文件失败: ${file.path}")
		}
		return result
	}

	fun recoveryFile(context: Context, file: FileData): FileData {
		Log.e("TAG", "recoveryFile: $file" )
		if(file.path.endsWith(".jpg") && !file.path.endsWith(".jpeg") && !file.path.endsWith(".png") && !file.path.endsWith(".gif") && !file.path.endsWith(".bmp") && !file.path.endsWith(".mp4") && !file.path.endsWith(".avi") && !file.path.endsWith(".mkv") && !file.path.endsWith(".mov") && !file.path.endsWith(".wmv")){
			// 图片，视频 才需要恢复到MediaStore
			val values = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
				put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.path))
				put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Recovered")
				put(MediaStore.MediaColumns.IS_PENDING, 1)
			}
			val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
			uri?.let {
				context.contentResolver.openOutputStream(it).use { outputStream ->
					val sourceFile = File(file.path)
					sourceFile.inputStream().use { inputStream ->
						inputStream.copyTo(outputStream!!)
					}
				}
				values.clear()
				values.put(MediaStore.MediaColumns.IS_PENDING, 0)
				context.contentResolver.update(uri, values, null, null)
			}

			val newFile = uri?.let { uri ->
				val cursor = context.contentResolver.query(uri, null, null, null, null)
				cursor?.use {
					if (it.moveToFirst()) {
						val idIndex = it.getColumnIndex(MediaStore.MediaColumns._ID)
						val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
						val sizeIndex = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
						val dateModifiedIndex = it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
						val path = uri.toString()
						val name = if (displayNameIndex != -1) it.getString(displayNameIndex) else file.name
						val size = if (sizeIndex != -1) it.getLong(sizeIndex) else file.size
						val lastModified = if (dateModifiedIndex != -1) it.getLong(dateModifiedIndex) * 1000 else file.lastModified
						FileData(name, path, size, lastModified)
					} else {
						file
					}
				} ?: file
			} ?: file
			delFile(file)
			return newFile
		} else {
			val extension = file.path.substringAfterLast('.', "").lowercase()
			// 根据文件类型设置目标路径
			val targetDir = when {
				extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "json") -> {
					// 文档类
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/RecoveredDocs"
				}
				extension in listOf("mp3", "wav", "aac", "flac") -> {
					// 音频类
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/RecoveredAudios"
				}
				extension in listOf("zip", "rar", "7z", "tar", "gz") -> {
					// 压缩包
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/RecoveredArchives"
				}
				else -> {
					// 其他类型，放在下载目录
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/RecoveredOthers"
				}
			}

			// 创建目标目录（如果不存在）
			val targetDirFile = File(targetDir)
			if (!targetDirFile.exists()) {
				val created = targetDirFile.mkdirs()
				if (!created) {
					Log.e("RecoveryRepository", "创建目录失败: $targetDir")
					return file
				}
			}

			// 复制文件到目标路径
			val targetFile = File(targetDirFile, file.name)
			val mFile = File(file.path)
			mFile.copyTo(targetFile, overwrite = true)

			delFile(file)

			return FileData(targetFile.name, targetFile.absolutePath, targetFile.length(), targetFile.lastModified())
		}
	}

	// 获取文件的MIME类型
	fun getMimeType(filePath: String): String {
		val extension = filePath.substringAfterLast('.', "").lowercase()
		return when (extension) {
			"jpg", "jpeg" -> "image/jpeg"
			"png" -> "image/png"
			"gif" -> "image/gif"
			"bmp" -> "image/bmp"
			"mp4" -> "video/mp4"
			"avi" -> "video/x-msvideo"
			"mkv" -> "video/x-matroska"
			"mov" -> "video/quicktime"
			"wmv" -> "video/x-ms-wmv"
			else -> "application/octet-stream"
		}
	}

	suspend fun saveRecoveredFiles(scanType: String, recoveredFiles: List<FileData>) {
		Log.e("TAG", "saveRecoveredFiles: scanType: $scanType, recoveredFiles: ${recoveredFiles.size}", )
		val recdFiles = getRecoveredFiles(scanType) + recoveredFiles
			.distinctBy { it.path }
		val key = stringPreferencesKey("recovered_$scanType")
		val jsonString = Json.encodeToString(recdFiles)
		dataStore.edit { prefs ->
			prefs[key] = jsonString
		}
	}

	suspend fun resetRecoveredFiles(scanType: String, recoveredFiles: List<FileData>) {
		val recdFiles = recoveredFiles
			.distinctBy { it.path }
		val key = stringPreferencesKey("recovered_$scanType")
		val jsonString = Json.encodeToString(recdFiles)
		dataStore.edit { prefs ->
			prefs[key] = jsonString
		}
	}

	suspend fun getRecoveredFiles(scanType: String): List<FileData> {
		val key = stringPreferencesKey("recovered_$scanType")
		return dataStore.data.map { prefs ->
			Json.decodeFromString<List<FileData>>(prefs[key] ?: "[]")
		}.firstOrNull() ?: emptyList()
	}

	suspend fun delRecoveredFiles(scanType: String, deletedFiles: List<FileData>) {
		val recoveredFiles = getRecoveredFiles(scanType)
		val savedFiles = recoveredFiles.filterNot { recoveredFile ->
			deletedFiles.any { it.path == recoveredFile.path }
		}
		resetRecoveredFiles(scanType, savedFiles)
		deletedFiles.forEach {
			val success = delFile(it)
			if (!success) {
				Log.e("TAG", "删除文件失败: ${it.path}")
			}
		}
	}
}