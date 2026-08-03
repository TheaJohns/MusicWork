package com.musicplayer.core.domain.player

import com.musicplayer.core.domain.model.Song
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 播放控制器接口（ARCH §3.4）。
 *
 * 设计要点：
 * - 状态/进度/当前曲以 [StateFlow] 暴露，列表与播放栏各自订阅同一可信源；
 * - 一次性错误事件以 [SharedFlow] 暴露（供 UI 弹提示，消费后即丢弃）；
 * - 播放状态与 Repository 的「列表数据」解耦，二者可独立订阅（ARCH §3.5 职责边界）。
 */
interface PlayerController {
    /** 当前播放状态（IDLE / PLAYING / PAUSED / ERROR） */
    val state: StateFlow<PlayerState>

    /** 当前播放进度(ms) */
    val currentPosition: StateFlow<Long>

    /** 当前歌曲总时长(ms) */
    val duration: StateFlow<Long>

    /** 当前歌曲 */
    val currentSong: StateFlow<Song?>

    /** 一次性错误事件（供 UI 弹提示） */
    val errorEvent: SharedFlow<PlayerError>

    /** 设置播放队列并从 startIndex 起播（默认 0） */
    fun setQueue(songs: List<Song>, startIndex: Int = 0)

    /** 从指定索引开始播放（列表项点击触发，US-3 / AC-3.3） */
    fun playAt(index: Int)

    fun play()
    fun pause()

    /** 错误态重试：以当前队列索引重建并重新播放（US-7 / AC-7.2 兜底） */
    fun retry()

    /** 下一首：队列尾保持尾曲（不循环，D3） */
    fun next()

    /** 上一首：队列头保持头曲（不循环，D3） */
    fun prev()

    /** 拖动进度跳转（US-5） */
    fun seekTo(positionMs: Long)

    /** 释放资源（绑定 Lifecycle / Service 销毁时调用） */
    fun release()
}
