package com.pdffox.adv.use.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object PreferenceUtil {
	private var sharedPreferences: SharedPreferences? = null
	private var editor: SharedPreferences.Editor? = null

	private const val TAG = "PreferenceUtil"

	fun init(context: Context) {
		if (sharedPreferences == null) sharedPreferences =
			context.getSharedPreferences("preference", Context.MODE_PRIVATE)
	}


	fun removeByKey(key: String?) {
		editor = sharedPreferences!!.edit()
		editor!!.remove(key)
		editor!!.commit()
	}

	fun removeAll() {
		editor = sharedPreferences!!.edit()
		editor!!.clear()
		editor!!.commit()
	}

	fun commitString(key: String?, value: String?) {
		editor = sharedPreferences!!.edit()
		editor!!.putString(key, value)
		editor!!.commit()
	}

	fun getString(key: String?, failValue: String?): String? {
		return sharedPreferences!!.getString(key, failValue)
	}

	fun commitInt(key: String?, value: Int) {
		editor = sharedPreferences!!.edit()
		editor!!.putInt(key, value)
		editor!!.commit()
	}

	fun getInt(key: String?, failValue: Int): Int {
		return sharedPreferences!!.getInt(key, failValue)
	}

	fun commitLong(key: String?, value: Long) {
		editor = sharedPreferences!!.edit()
		editor!!.putLong(key, value)
		editor!!.commit()
	}

	fun getLong(key: String?, failValue: Long): Long {
		return sharedPreferences!!.getLong(key, failValue)
	}

	fun commitBoolean(key: String?, value: Boolean) {
		editor = sharedPreferences!!.edit()
		editor!!.putBoolean(key, value)
		editor!!.commit()
	}

	fun getBoolean(key: String?, failValue: Boolean): Boolean {
		return sharedPreferences!!.getBoolean(key, failValue)
	}

	fun commitDouble(key: String?, value: Double) {
		editor = sharedPreferences!!.edit()
		editor!!.putString(key, value.toString() + "")
		editor!!.commit()
	}

	fun getDouble(key: String?, failValue: Double): Double {
		val strValue: String = sharedPreferences!!.getString(key, "")!!
		if (strValue.isEmpty()) return failValue
		return strValue.toDouble()
	}

	fun commitFloat(key: String?, value: Float) {
		editor = sharedPreferences!!.edit()
		editor!!.putFloat(key, value)
		editor!!.commit()
	}

	fun getFloat(key: String?, failValue: Float): Float {
		return sharedPreferences!!.getFloat(key, failValue)
	}
}

class PreferenceDelegate<T>(
	private val key: String,
	private val defaultValue: T
) : ReadWriteProperty<Any?, T> {

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return when (defaultValue) {
			is Int -> PreferenceUtil.getInt(key, defaultValue) as T
			is Long -> PreferenceUtil.getLong(key, defaultValue) as T
			is Boolean -> PreferenceUtil.getBoolean(key, defaultValue) as T
			is Float -> PreferenceUtil.getFloat(key, defaultValue) as T
			is String -> PreferenceUtil.getString(key, defaultValue) as T
			is Double -> PreferenceUtil.getDouble(key, defaultValue) as T
			else -> throw IllegalArgumentException("Unsupported type.")
		}
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		when (value) {
			is Int -> PreferenceUtil.commitInt(key, value)
			is Long -> PreferenceUtil.commitLong(key, value)
			is Boolean -> PreferenceUtil.commitBoolean(key, value)
			is Float -> PreferenceUtil.commitFloat(key, value)
			is String -> PreferenceUtil.commitString(key, value)
			is Double -> PreferenceUtil.commitDouble(key, value)
			else -> throw IllegalArgumentException("Unsupported type.")
		}
	}
}