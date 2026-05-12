package com.datatool.photorecovery.core.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri

class PermissionRepository(private val context: Context) {

	fun checkNotificationPermission(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			// Android 13 及以上需要动态通知权限
			context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
		} else {
			// 低版本默认允许通知权限
			NotificationManagerCompat.from(context).areNotificationsEnabled()
		}
	}

	fun requestNotificationPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
		}
	}

	fun checkFileManagerPermission(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			// Android 11 及以上，检查是否有所有文件访问权限
			Environment.isExternalStorageManager()
		} else {
			// Android 10 及以下，检查读写权限
			val readPermission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
			val writePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
			readPermission && writePermission
		}
	}

	fun requestFileManagerPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
		Log.e("TAG", "requestFileManagerPermission: ", )
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (!Environment.isExternalStorageManager()) {
				val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
				intent.data = ("package:" + context.packageName).toUri()
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				context.startActivity(intent)
			}
		} else {
			requestPermissionLauncher.launch(arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			))
		}
	}

}