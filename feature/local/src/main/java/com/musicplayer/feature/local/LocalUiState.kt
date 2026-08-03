package com.musicplayer.feature.local

import com.musicplayer.core.domain.model.Song

/** 本地权限状态机（ARCH §3.6 / US-6） */
enum class PermissionState {
    UNDETERMINED,        // 尚未请求
    GRANTED,             // 已授权
    DENIED,              // 拒绝（可再次请求）
    DENIED_PERMANENTLY   // 拒绝且「不再询问」→ 引导去设置（AC-6.2）
}

/**
 * 本地音乐页 UI 状态（ARCH §3.6 / MVVM + UDF）。
 */
data class LocalUiState(
    val permissionState: PermissionState = PermissionState.UNDETERMINED,
    val isLoading: Boolean = false,    // 扫描中
    val songs: List<Song> = emptyList(), // 本地歌曲
    val error: String? = null          // 错误信息
)
