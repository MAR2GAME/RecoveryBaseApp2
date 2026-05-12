package com.pdffox.adv.update

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

object AppUpdateHelper {

	private const val TAG = "AppUpdateHelper"
	private const val UPDATE_REQUEST_CODE = 4001

	fun forceImmediateUpdate(activity: Activity) {
		val appUpdateManager = AppUpdateManagerFactory.create(activity)
		appUpdateManager.appUpdateInfo
			.addOnSuccessListener { appUpdateInfo ->
				when (appUpdateInfo.updateAvailability()) {
					UpdateAvailability.UPDATE_AVAILABLE -> {
						if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
							startImmediateUpdate(appUpdateManager, appUpdateInfo, activity)
						} else {
							openStore(activity)
						}
					}
					UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
						startImmediateUpdate(appUpdateManager, appUpdateInfo, activity)
					}
					else -> {
						openStore(activity)
					}
				}
			}
			.addOnFailureListener { exception ->
				Log.e(TAG, "forceImmediateUpdate: failed to query availability", exception)
				openStore(activity)
			}
	}

	private fun startImmediateUpdate(
		appUpdateManager: AppUpdateManager,
		appUpdateInfo: AppUpdateInfo,
		activity: Activity,
	) {
		try {
			val flowStarted = appUpdateManager.startUpdateFlowForResult(
				appUpdateInfo,
				activity,
				AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
					.setAllowAssetPackDeletion(true)
					.build(),
				UPDATE_REQUEST_CODE,
			)
			if (!flowStarted) {
				openStore(activity)
			}
		} catch (intentException: IntentSender.SendIntentException) {
			Log.e(TAG, "startImmediateUpdate: unable to start flow", intentException)
			openStore(activity)
		}
	}

	private fun openStore(activity: Activity) {
		val packageName = activity.packageName
		val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
		try {
			activity.startActivity(marketIntent)
		} catch (activityNotFound: ActivityNotFoundException) {
			val webIntent = Intent(
				Intent.ACTION_VIEW,
				Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
			).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
			activity.startActivity(webIntent)
		} finally {
			activity.finishAffinity()
		}
	}
}
