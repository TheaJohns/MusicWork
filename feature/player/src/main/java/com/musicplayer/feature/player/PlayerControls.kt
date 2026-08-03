package com.musicplayer.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.core.domain.player.PlayerState
import com.musicplayer.core.ui.utils.formatMMSS

/**
 * 进度 Slider（§2.4 / §6.6）。
 *
 * - valueRange = 0f..1f，显示值 = 进度/时长；
 * - 拖动中记录 [dragFraction]，松手 via [onSeek] 提交（避免拖动时进度回弹，§4.3）；
 * - 左时间拖动中显示 dragFraction 对应位置，松手恢复实时位置；
 * - 无障碍：ProgressBarRangeInfo 语义，TalkBack 朗读百分比。
 *
 * @param currentPositionMs 当前进度(ms)
 * @param durationMs 总时长(ms)
 * @param onSeek 松手提交跳转目标(ms)
 */
@Composable
fun PlayerProgress(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 拖动态（本地态，不污染全局状态）
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(0f) }

    val safeDuration = if (durationMs > 0) durationMs else 0L
    val displayFraction = if (isDragging) {
        dragFraction
    } else if (safeDuration > 0) {
        (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    } else {
        0f
    }
    val displayedPosition = if (isDragging) (dragFraction * safeDuration).toLong() else currentPositionMs

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatMMSS(displayedPosition),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = displayFraction,
            onValueChange = {
                isDragging = true
                dragFraction = it
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek((dragFraction * safeDuration).toLong())
            },
            enabled = safeDuration > 0,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .semantics {
                    contentDescription = "播放进度"
                    progressBarRangeInfo = ProgressBarRangeInfo(displayFraction, 0f..1f)
                },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Text(
            text = formatMMSS(safeDuration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 播放控制行（§2.4 / §6.4）。
 *
 * - 上一首 / 下一首：AutoMirrored 图标（兼容 RTL），[enabled] 控制可用性；
 * - 播放/暂停：FilledIconButton（primary 容器强调），按 [state] 切换图标；
 * - [expanded] 时放大图标（MiniPlayer 默认尺寸 / PlayerScreen 放大）。
 *
 * @param state 当前播放状态（决定播放/暂停图标）
 * @param enabled 是否有当前歌曲（currentSong==null 时全部禁用）
 */
@Composable
fun PlayerControls(
    state: PlayerState,
    enabled: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    val playIconSize = if (expanded) 36.dp else 24.dp
    val sideIconSize = if (expanded) 32.dp else 24.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrev,
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "上一首",
                modifier = androidx.compose.ui.Modifier.size(sideIconSize)
            )
        }
        FilledIconButton(
            onClick = onPlayPause,
            enabled = enabled
        ) {
            Icon(
                imageVector = if (state == PlayerState.PLAYING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (state == PlayerState.PLAYING) "暂停" else "播放",
                modifier = Modifier.size(playIconSize)
            )
        }
        IconButton(
            onClick = onNext,
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "下一首",
                modifier = Modifier.size(sideIconSize)
            )
        }
    }
}

/**
 * 播放错误横幅（§6.7 备选方案，自包含不依赖 SnackbarHost）。
 * errorContainer 底色 + 「暂时无法播放」+ 内联「下一首 / 重试」。
 * 不阻塞其它歌曲/列表（AC-7.2）。
 */
@Composable
fun PlayerErrorBanner(
    onNext: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "暂时无法播放",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onNext) {
                Text(
                    text = "下一首",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onRetry) {
                Text(
                    text = "重试",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
