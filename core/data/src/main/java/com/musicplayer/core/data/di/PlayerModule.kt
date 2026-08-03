package com.musicplayer.core.data.di

import com.musicplayer.core.data.player.PlayerControllerImpl
import com.musicplayer.core.domain.player.PlayerController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 播放控制器 Hilt 模块（ARCH §1.7）。
 * PlayerController 以 @Singleton 注入，跨列表/播放栏共享同一播放状态。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindPlayerController(impl: PlayerControllerImpl): PlayerController
}
