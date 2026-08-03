package com.musicplayer.core.domain.player

import com.musicplayer.core.domain.model.Song

/**
 * 播放队列（ARCH §3.4 / D3）。
 *
 * 边界策略（D3 / R6）：
 * - 队列头「上一首」保持头曲（不循环）；
 * - 队列尾「下一首」保持尾曲（不循环）；
 * - 采用 clamp 而非取模，确保不会越界。
 */
data class PlaybackQueue(
    val songs: List<Song>,
    val currentIndex: Int = 0
) {
    /** 当前曲目（越界返回 null） */
    fun current(): Song? = songs.getOrNull(currentIndex)

    /** 下一首索引：未到队尾则 +1，否则保持当前（尾保持） */
    fun nextIndex(): Int =
        if (currentIndex < songs.lastIndex) currentIndex + 1 else currentIndex

    /** 上一首索引：未到队头则 -1，否则保持当前（头保持） */
    fun prevIndex(): Int =
        if (currentIndex > 0) currentIndex - 1 else currentIndex

    /** 队列是否非空 */
    fun hasSongs(): Boolean = songs.isNotEmpty()
}
