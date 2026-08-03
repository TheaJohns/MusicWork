package com.musicplayer.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.model.SongSource
import com.musicplayer.core.ui.utils.formatMMSS

/**
 * 统一歌曲列表项（在线 / 本地共用，§6.2）。
 *
 * 依 [Song.source] 决定尾部内容：
 * - 在线（ONLINE）：尾部显示**来源 Chip**（由 [Song.platform] 映射友好名）；
 * - 本地（LOCAL）：尾部显示**时长** `mm:ss`，副标题显示 `歌手 · 专辑`。
 *
 * 整行 `clickable` + `role = Button` + 内容描述，使读屏识别为可播放按钮（§4.4）。
 *
 * @param song 歌曲领域模型
 * @param onClick 点击整行（触发播放）
 * @param coverSize 封面边长，默认 56.dp
 */
@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverSize: Dp = 56.dp
) {
    val subtitle = if (song.source == SongSource.ONLINE) {
        song.artist
    } else {
        if (!song.album.isNullOrBlank()) "${song.artist} · ${song.album}" else song.artist
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "播放《${song.title}》${song.artist}",
                onClick = onClick
            )
            .semantics { contentDescription = "播放《${song.title}》${song.artist}" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 封面（§6.5：Coil + MusicNote 占位）
        SongCoverImage(
            url = song.coverUrl,
            size = coverSize,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            contentDescription = "《${song.title}》封面"
        )

        // 主信息列
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 尾部：在线→来源 Chip；本地→时长
        when (song.source) {
            SongSource.ONLINE -> SourceChip(label = sourceLabel(song.platform, song.source))
            SongSource.LOCAL -> Text(
                text = formatMMSS(song.durationMs ?: 0L),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 来源标签（静态非交互 Chip，§6.3）。
 * 用 disabled 态保留品牌色（避免点击波纹/交互语义），仅作信息展示。
 */
@Composable
private fun SourceChip(label: String) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        modifier = Modifier.padding(start = 8.dp)
    )
}

/**
 * platform → 友好标签映射（§6.3 / 与 qa 锁定的最终契约）。
 *
 * 规则：
 * - 已知平台（netease/qq/kugou/kuwo/xiami/baidu）→ 对应中文名；
 * - 未知但非空 → 统一回退「在线」（来源 Chip 仅对在线曲渲染，保持干净且确定）；
 * - platform 为空/空白 → 按来源回退：本地「本地」、在线「在线」。
 *
 * 此函数为纯函数，供 UI 渲染与单测断言共用。
 */
fun sourceLabel(platform: String?, source: SongSource): String {
    if (!platform.isNullOrBlank()) {
        return when (platform.lowercase()) {
            "netease" -> "网易云"
            "qq" -> "QQ音乐"
            "kugou" -> "酷狗"
            "kuwo" -> "酷我"
            "xiami" -> "虾米"
            "baidu" -> "百度音乐"
            else -> "在线"
        }
    }
    return if (source == SongSource.LOCAL) "本地" else "在线"
}
