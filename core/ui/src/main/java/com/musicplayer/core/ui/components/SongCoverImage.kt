package com.musicplayer.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * 封面图（Coil 封装，符合 ARCH §1.5：自动内存+磁盘缓存、占位图、错误图）。
 *
 * 在线 pic URL 与本地专辑封面 content URI 统一走 Coil（§6.5）。
 * 占位图 / 错误图无需二进制资源，直接用内置 [Icons.Filled.MusicNote] 矢量图标，
 * 配合主题 `surfaceVariant` 底色即可满足 PRD §3.3 封面失败兜底。
 *
 * @param url 封面地址（在线 URL 或本地 content URI），为 null 时直接展示占位
 * @param size 封面边长（列表 56.dp / MiniPlayer 48.dp / 播放页 280.dp）
 * @param shape 圆角（列表 8.dp / MiniPlayer 12.dp / 播放页 16.dp）
 * @param contentDescription 无障碍描述，如「《歌名》封面」
 */
@Composable
fun SongCoverImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(shape),
        contentScale = ContentScale.Crop,
        placeholder = rememberVectorPainter(Icons.Filled.MusicNote),
        error = rememberVectorPainter(Icons.Filled.MusicNote)
    )
}
