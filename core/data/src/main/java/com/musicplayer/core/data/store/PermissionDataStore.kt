package com.musicplayer.core.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 存储权限「不再询问」标记的持久化（ARCH §1.8 / US-6 / AC-6.2）。
 * 读取该标记可判断是否需要引导用户去「设置」开启权限。
 */
@Singleton
class PermissionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val neverAskAgainKey =
        booleanPreferencesKey("local_audio_permission_never_ask_again")

    /** 读取「不再询问」标记（默认 false） */
    val neverAskAgain: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[neverAskAgainKey] ?: false
    }

    /** 写入「不再询问」标记 */
    suspend fun setNeverAskAgain(value: Boolean) {
        dataStore.edit { prefs -> prefs[neverAskAgainKey] = value }
    }
}
