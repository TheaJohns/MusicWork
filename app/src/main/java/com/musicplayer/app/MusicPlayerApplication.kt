package com.musicplayer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口（@HiltAndroidApp 触发 Hilt 代码生成与组件装配）。
 * Hilt 模块（NetworkModule / DataModule / PlayerModule）经 @InstallIn(SingletonComponent)
 * 自动装配，全局单例（Retrofit、Repository、PlayerController 等）在此生根。
 */
@HiltAndroidApp
class MusicPlayerApplication : Application()
