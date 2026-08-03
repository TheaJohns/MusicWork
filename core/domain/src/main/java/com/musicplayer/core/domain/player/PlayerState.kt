package com.musicplayer.core.domain.player

import com.musicplayer.core.domain.model.Song

/** 播放状态（ARCH §3.4） */
enum class PlayerState {
    IDLE,    // 未加载 / 已停止
    PLAYING, // 播放中
    PAUSED,  // 暂停
    ERROR    // 播放失败
}

/** 播放错误类型，用于区分兜底策略（ARCH §3.4 / US-7） */
enum class PlayerErrorType {
    NETWORK,        // 网络失败（无网 / 超时 / 连接失败）
    SOURCE_INVALID, // 外链失效（403/404 等）
    DECODE,         // 解码失败
    UNKNOWN         // 其它未知错误
}

/**
 * 一次性播放错误信息，经 SharedFlow 抛给 UI 弹提示（不进 StateFlow，避免状态残留）。
 */
data class PlayerError(
    val song: Song?,      // 出错的歌曲（便于做「下一首/重试」）
    val type: PlayerErrorType,
    val message: String?
)
