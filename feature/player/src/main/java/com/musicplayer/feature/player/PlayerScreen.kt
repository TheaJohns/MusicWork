package com.musicplayer.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicplayer.core.domain.player.PlayerState
import com.musicplayer.core.ui.components.SongCoverImage

/**
 * 全屏播放页（覆盖层，§2.6 / §6.1 → PlayerScreen）。
 *
 * 由宿主以 AnimatedVisibility + slideInVertically 包裹呈现，z 序最高；
 * 仅当 currentSong != null 时可展开（宿主 gate）。
 * 展示：大封面 + 歌名/歌手 + 进度 Slider + 控制行 + 静态歌词（空则「暂无歌词」）+ 错误横幅。
 *
 * @param viewModel 共享播放 ViewModel（默认 hiltViewModel，与宿主同实例）
 * @param onCollapse 点击收起按钮收回覆盖层
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val song = uiState.currentSong

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("正在播放", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onCollapse) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "收起播放页"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 播放错误横幅（不阻塞其它歌曲，§6.7）
                uiState.error?.let {
                    PlayerErrorBanner(
                        onNext = viewModel::onNext,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.onRetry()
                        }
                    )
                }

                if (song != null) {
                    // 大封面
                    SongCoverImage(
                        url = song.coverUrl,
                        size = 280.dp,
                        shape = RoundedCornerShape(16.dp),
                        contentDescription = "《${song.title}》封面",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    // 标题 / 歌手
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // 进度 + 控制
                    PlayerProgress(
                        currentPositionMs = uiState.currentPositionMs,
                        durationMs = uiState.durationMs,
                        onSeek = viewModel::onSeek
                    )
                    PlayerControls(
                        state = uiState.state,
                        enabled = true,
                        onPlayPause = {
                            if (uiState.state == PlayerState.PLAYING) viewModel.onPause() else viewModel.onPlay()
                        },
                        onPrev = viewModel::onPrev,
                        onNext = viewModel::onNext,
                        expanded = true
                    )
                    // 歌词（静态文本，R8；空则占位，PRD §3.3）
                    if (!song.lyric.isNullOrBlank()) {
                        Text(
                            text = song.lyric!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "暂无歌词",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "未在播放", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
