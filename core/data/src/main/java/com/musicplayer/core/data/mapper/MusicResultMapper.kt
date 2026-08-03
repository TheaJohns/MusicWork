package com.musicplayer.core.data.mapper

import com.musicplayer.core.data.remote.MusicItemDto
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.model.SongSource

/**
 * DTO → 领域模型映射（ARCH §3.2 映射规则）。
 *
 * 规则：
 * - [MusicItemDto.url] 为空/空白 → 过滤丢弃（R1，避免无效歌曲）；
 * - 去重：优先按 [MusicItemDto.songid]，缺失时按 url；
 * - 任意字段缺失使用占位值，不抛异常（R2 容错）；
 * - album 在线接口无此字段，固定为 null；durationMs 在线不返回，固定为 null。
 */
object MusicResultMapper {

    /** 将响应列表映射为去重后的 Song 列表（保留原始顺序） */
    fun map(response: List<MusicItemDto>): List<Song> {
        val seen = LinkedHashSet<String>() // 保证顺序的去重集合
        return response.mapNotNull { dto ->
            val url = dto.url
            // R1：播放地址为空 → 过滤丢弃
            if (url.isNullOrBlank()) return@mapNotNull null

            // 去重键：优先 songid，缺失按 url
            val dedupKey = dto.songid?.takeIf { it.isNotBlank() } ?: url
            if (!seen.add(dedupKey)) return@mapNotNull null // 已存在则跳过

            Song(
                id = dto.songid?.takeIf { it.isNotBlank() } ?: url,
                title = dto.title?.takeIf { it.isNotBlank() } ?: "未知歌曲",
                artist = dto.author?.takeIf { it.isNotBlank() } ?: "未知歌手",
                album = null,
                coverUrl = dto.pic,
                platform = dto.type,
                durationMs = null,
                source = SongSource.ONLINE,
                playUrl = url,
                lyric = dto.lrc,
                originLink = dto.link
            )
        }
    }
}
