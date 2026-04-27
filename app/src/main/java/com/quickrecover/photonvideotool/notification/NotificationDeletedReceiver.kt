//package com.quickrecover.photonvideotool.notification
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//
//class NotificationDeletedReceiver : BroadcastReceiver() {
//	companion object {
//		const val TAG = "NotificationDeletedRece"
//	}
//	override fun onReceive(context: Context?, intent: Intent?) {
//		Log.e(TAG, "onReceive: ", )
//		context?.let {
//			val serviceIntent = Intent(it, CommonService::class.java)
//			it.startForegroundService(serviceIntent)
//		}
//	}
//}