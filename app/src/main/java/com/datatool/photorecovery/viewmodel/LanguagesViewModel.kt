package com.datatool.photorecovery.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class LanguagesViewModel : ViewModel(), KoinComponent {
	private val dataStore: DataStore<Preferences> by inject()
	private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

	private val _selectedLanguage = MutableStateFlow<String?>(null)
	val selectedLanguage = _selectedLanguage

	val LANGUAGE_MAP: Map<String, Locale> = mapOf(
		"العربية" to Locale("ar"),
		"čeština" to Locale("cs"),
		"dansk" to Locale("da"),
		"Deutsch" to Locale("de"),
		"Eλληνικά" to Locale("el"),
		"English" to Locale.ENGLISH,
		"español" to Locale("es"),
		"svenska" to Locale("sv"),
		"français" to Locale.FRENCH,
		"Indonesia" to Locale("id"),
		"italiano" to Locale.ITALIAN,
		"日本語" to Locale.JAPANESE,
		"한국어" to Locale.KOREAN,
		"Nederlands" to Locale("nl"),
		"norsk" to Locale("no"),
		"polski" to Locale("pl"),
		"português" to Locale("pt"),
		"română" to Locale("ro"),
		"Türkçe" to Locale("tr"),
		"中文 (简体)" to Locale("zh", "CN"),
		"中文 (繁体)" to Locale("zh", "TW")
	)

	suspend fun loadSelectedLanguage() {
		val displayName = dataStore.data.map { it[LANGUAGE_KEY] }.first()
		_selectedLanguage.value = displayName ?: "English"
	}

	fun selectLanguage(languageName: String) {
		_selectedLanguage.value = languageName
	}

	// 更新设置语言的方法
	suspend fun setLocale(languageDisplayName: String?) {
		dataStore.edit { preferences ->
			if (languageDisplayName != null) {
				preferences[LANGUAGE_KEY] = languageDisplayName
			} else {
				preferences.remove(LANGUAGE_KEY)
			}
		}

		val locale = LANGUAGE_MAP[languageDisplayName]
		val localeList = if (locale != null) {
			LocaleListCompat.create(locale)
		} else {
			LocaleListCompat.getEmptyLocaleList()
		}
		AppCompatDelegate.setApplicationLocales(localeList)
	}

	suspend fun getCurrentLocale(): Locale {
		val displayName = dataStore
			.data
			.map { preferences ->
				preferences[LANGUAGE_KEY]
			}
			.first()
		return LANGUAGE_MAP[displayName] ?: Locale.getDefault()
	}

}
