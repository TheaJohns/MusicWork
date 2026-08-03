package com.musicplayer.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.musicplayer.core.data.local.LocalMusicDataSource
import com.musicplayer.core.data.repository.MusicRepositoryImpl
import com.musicplayer.core.data.store.PermissionDataStore
import com.musicplayer.core.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据层 Hilt 模块（ARCH §1.7）。
 *
 * - [bindMusicRepository]：将 Repository 接口绑定到实现；
 * - 提供 [LocalMusicDataSource]、DataStore<Preferences> 与 [PermissionDataStore]。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /** 将 MusicRepository 接口绑定到 MusicRepositoryImpl 单例 */
    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    companion object {
        @Provides
        @Singleton
        fun provideLocalMusicDataSource(
            @ApplicationContext context: Context
        ): LocalMusicDataSource = LocalMusicDataSource(context)

        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = androidx.datastore.preferences.core.PreferenceDataStoreFactory
            .create { context.preferencesDataStoreFile("music_player_prefs") }

        @Provides
        @Singleton
        fun providePermissionDataStore(
            dataStore: DataStore<Preferences>
        ): PermissionDataStore = PermissionDataStore(dataStore)
    }
}
