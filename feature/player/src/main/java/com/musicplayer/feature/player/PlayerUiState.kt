package com.musicplayer.feature.player

import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.player.PlayerError
import com.musicplayer.core.domain.player.PlayerState

/**
 * 播放栏 / 播放页 UI 状态（ARCH §3.6 / MVVM + UDF）。
 * 状态、进度、当前曲、一次性错误均来自 [com.musicplayer.core.domain.player.PlayerController]。
 */
data class PlayerUiState(
    val currentSong: Song? = null,      // 当前歌曲
    val state: PlayerState = PlayerState.IDLE, // 播放状态
    val currentPositionMs: Long = 0,    // 当前进度(ms)
    val durationMs: Long = 0,           // 总时长(ms)
    val error: PlayerError? = null      // 一次性错误（非空 → 提示「暂时无法播放」）
)
