package com.musicplayer.core.domain.model

/** 歌曲来源枚举：在线外链 / 本地媒体库 */
enum class SongSource {
    ONLINE, LOCAL
}

/**
 * 统一歌曲领域模型（对齐 PRD §3.1.3 / ARCH §3.1）。
 * 在线与本地歌曲共用此模型，列表与播放队列都以它为唯一数据形态，
 * 从而满足「统一模型、统一队列」的设计（D2 / D3）。
 */
data class Song(
    val id: String,          // 唯一标识：在线 = songid，本地 = MediaStore _ID
    val title: String,       // 歌名（主标题）
    val artist: String,      // 歌手 / 艺术家
    val album: String?,      // 专辑（在线多为 null）
    val coverUrl: String?,   // 封面：在线 = pic URL，本地 = 专辑封面 content URI
    val platform: String? = null, // 来源平台标识（在线，如 netease/qq/kugou），用于 UI 来源标签映射
    val durationMs: Long?,   // 时长(ms)：本地有，在线多数 null（播时由播放器取）
    val source: SongSource,  // 来源
    val playUrl: String,     // 播放地址：在线 = url 外链，本地 = content:// URI
    val lyric: String?,      // 歌词文本：仅在线有，可空
    val originLink: String?  // 原站链接：仅在线有
)
