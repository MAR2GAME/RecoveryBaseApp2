package com.datatool.photorecovery.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.datatool.photorecovery.core.model.PermissionRepository
import com.datatool.photorecovery.core.model.RecoveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import java.io.File

val permissionRepository = module {
	single { PermissionRepository(get()) }
}

val recoveryRepository = module {
	single { RecoveryRepository(get()) }
}

val dataStoreModule = module {
	single<DataStore<Preferences>> {
		PreferenceDataStoreFactory.create(
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
			produceFile = { File(get<Context>().filesDir, "settings.preferences_pb") }
		)
	}
}