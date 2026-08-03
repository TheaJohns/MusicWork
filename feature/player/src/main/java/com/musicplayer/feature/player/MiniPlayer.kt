package com.musicplayer.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicplayer.core.domain.player.PlayerState
import com.musicplayer.core.ui.components.SongCoverImage

/**
 * 底部常驻迷你播放栏（§2.4 / §6.1 → MiniPlayer）。
 *
 * - 主行：封面 + 歌名/歌手（点击主体展开播放页）+ 上一首/播放暂停/下一首；
 * - 进度行：当前时间 + Slider + 总时长；
 * - currentSong==null 时保持常驻禁用（高度不变，避免布局跳动）；
 * - 播放错误时顶部展示 errorContainer 横幅（§6.7）。
 *
 * 由宿主（MainActivity）放入 Scaffold.bottomBar。
 *
 * @param viewModel 共享播放 ViewModel（默认 hiltViewModel，与宿主同实例）
 * @param onExpand 点击主体展开全屏播放页（仅 currentSong!=null 时触发）
 */
@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel = hiltViewModel(),
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val song = uiState.currentSong
    val hasSong = song != null

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 播放错误横幅（不阻塞其它歌曲，§6.7）
            uiState.error?.let {
                PlayerErrorBanner(
                    onNext = viewModel::onNext,
                    onRetry = {
                        // 清除一次性错误后以当前曲真正重播（AC-7.2）
                        viewModel.clearError()
                        viewModel.onRetry()
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // 主行：封面 + 信息（可点击展开）+ 控制
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SongCoverImage(
                        url = song?.coverUrl,
                        size = 48.dp,
                        shape = RoundedCornerShape(12.dp),
                        contentDescription = song?.let { "《${it.title}》封面" }
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                            .clickable(
                                enabled = hasSong,
                                role = Role.Button,
                                onClickLabel = "展开播放页",
                                onClick = onExpand
                            )
                    ) {
                        Text(
                            text = song?.title ?: "未在播放",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song?.artist ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    PlayerControls(
                        state = uiState.state,
                        enabled = hasSong,
                        onPlayPause = {
                            if (uiState.state == PlayerState.PLAYING) viewModel.onPause() else viewModel.onPlay()
                        },
                        onPrev = viewModel::onPrev,
                        onNext = viewModel::onNext,
                        expanded = false
                    )
                }

                // 进度行
                PlayerProgress(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    onSeek = viewModel::onSeek,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}
