package com.mar2game.safe

import android.app.Activity
import android.content.Context
import android.widget.Toast
import kotlin.system.exitProcess
import android.os.Process
import android.util.Log

object SafeChecker {

	private const val TAG = "SafeChecker"

	fun checkAndShutDown(context: Context) {
		if (!check(context)) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "checkAndShutDown: Application is not in a safe state, shutting down...")
			}
			// 提示用户应用状态不安全
//			Toast.makeText(context, "Application is not in a safe state, shutting down...", Toast.LENGTH_LONG).show()

			// 1. 结束当前 Activity 和栈中的其他 Activity
			(context as? Activity)?.finishAffinity()

			// 2. 强制退出应用
			Process.killProcess(Process.myPid())
			exitProcess(0)  // 或者 Process.killProcess(Process.myPid())
		}
	}

	fun check(context: Context): Boolean {
		val checkPackageNameResult = checkPackageName(context)
//		val checkSignatureResult = checkSignature(context)
//		val checkDebugBuildResult = checkDebugBuild(context)
//		val checkIconResult = checkIcon(context)
//		val checkAntiDebugResult = checkAntiDebug()
		//check: true, true, false, true, false
		if (!checkPackageNameResult) {
			return false
		}
//		if (!checkSignatureResult) {
//			return false
//		}
//		if (checkDebugBuildResult) {
//			return false
//		}
//		if (!checkIconResult) {
//			return false
//		}
//		if (checkAntiDebugResult) {
//			return false
//		}
		return true
	}

	private fun checkPackageName(context: Context): Boolean {
		val expectedPackageName = "com.quickrecover.photonvideotool"
		// 获取当前应用的包名
		val actualPackageName = context.packageName
		// 比较预期包名与实际包名
		return expectedPackageName == actualPackageName
	}

	// keytool -list -v -keystore your_keystore_path -alias your_alias
	private fun checkSignature(context: Context): Boolean {
		val expectedSignature = "BC9226C0D24125D7BFF05CF3D746EFFCF72AB101E8B14BAFB1EB7C08557BECDC"

		return try {
			// 获取当前应用的包名
			val packageManager = context.packageManager
			val packageName = context.packageName

			// 获取包信息，包括签名信息
			val packageInfo = packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)

			// 获取签名证书的签名信息
			val signatures = packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()

			// 遍历所有签名，计算其 SHA-256 哈希并与预期签名比对
			signatures.any { signature ->
				val digest = java.security.MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
				val hexString = digest.joinToString("") { "%02X".format(it) }
				hexString.equals(expectedSignature, ignoreCase = true)
			}
		} catch (e: Exception) {
			false
		}
	}

	private fun checkDebugBuild(context: Context): Boolean {
		return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
	}

	private fun checkAntiDebug(): Boolean {
		// 1. 检测是否有调试器连接
		if (android.os.Debug.isDebuggerConnected()) {
			return true
		}

		// 2. 读取 /proc/self/status 文件，检查 TracerPid 字段
		return try {
			val tracerPid = java.io.File("/proc/self/status")
				.useLines { lines ->
					lines.firstOrNull { it.startsWith("TracerPid:") }
						?.split(":")?.get(1)?.trim()?.toIntOrNull()
				}

			// 如果 TracerPid 大于 0，表示进程有调试器附着
			tracerPid != null && tracerPid > 0
		} catch (e: Exception) {
			false
		}
	}


	// CertUtil -hashfile logo.png MD
	private fun checkIcon(context: Context): Boolean {
		val expectedMd5 = "edb69cfbee2d7e89cc0e87cc8c76b5ea"

		return try {
			// 获取当前应用的包管理器
			val applicationInfo = context.applicationInfo

			// 获取图标资源 ID
			val iconResId = applicationInfo.icon

			// 获取原始的图标文件流
			val resources = context.resources
			val iconInputStream = resources.openRawResource(iconResId)

			// 读取图标文件并计算 MD5
			val byteArrayOutputStream = java.io.ByteArrayOutputStream()
			val buffer = ByteArray(1024)
			var length: Int
			while (iconInputStream.read(buffer).also { length = it } != -1) {
				byteArrayOutputStream.write(buffer, 0, length)
			}
			iconInputStream.close()

			// 计算 MD5 校验码
			val bytes = byteArrayOutputStream.toByteArray()
			val md = java.security.MessageDigest.getInstance("MD5")
			val digest = md.digest(bytes)
			val md5String = digest.joinToString("") { "%02x".format(it) }

			// 与预期 MD5 值比对
			md5String.equals(expectedMd5, ignoreCase = true)
		} catch (e: Exception) {
			false
		}
	}


}