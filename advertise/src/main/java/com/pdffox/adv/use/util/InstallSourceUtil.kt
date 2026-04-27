package com.pdffox.adv.use.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

private val trustedInstallerPackages = setOf(
	"com.android.vending",
	"com.google.android.feedback",
)

object InstallSourceUtil {
	fun isTrustedStoreInstall(context: Context): Boolean {
		val packageManager = context.packageManager
		val installerPackage = try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				packageManager.getInstallSourceInfo(context.packageName).installingPackageName
			} else {
				@Suppress("DEPRECATION")
				packageManager.getInstallerPackageName(context.packageName)
			}
		} catch (_: IllegalArgumentException) {
			null
		} catch (_: PackageManager.NameNotFoundException) {
			null
		}
		return installerPackage in trustedInstallerPackages
	}
}
