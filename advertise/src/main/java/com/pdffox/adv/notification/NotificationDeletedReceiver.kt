package com.pdffox.adv.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pdffox.adv.Config

class NotificationDeletedReceiver : BroadcastReceiver() {
	companion object {
		const val TAG = "NotificationDeletedRece"
	}
	override fun onReceive(context: Context?, intent: Intent?) {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.notificationDeletedReceiverEnabled) {
			return
		}
		Log.e(TAG, "onReceive: ", )
		context?.let(CommonService::start)
	}
}
