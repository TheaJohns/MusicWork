package com.musicplayer.feature.search

import com.musicplayer.core.domain.model.Song

/**
 * 在线搜索页 UI 状态（ARCH §3.6 / MVVM + UDF）。
 * StateFlow 单一可信源，UI 仅订阅并派发意图给 ViewModel。
 */
data class SearchUiState(
    val keyword: String = "",         // 搜索输入框内容
    val isLoading: Boolean = false,    // 加载中
    val songs: List<Song> = emptyList(), // 搜索结果
    val error: String? = null,         // 错误信息（非空即展示失败态）
    val isEmpty: Boolean = false       // result 为空 → 「未找到相关歌曲」空态
)
