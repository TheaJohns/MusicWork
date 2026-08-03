package com.musicplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicplayer.core.ui.theme.MusicPlayerTheme
import com.musicplayer.feature.local.LocalScreen
import com.musicplayer.feature.player.MiniPlayer
import com.musicplayer.feature.player.PlayerScreen
import com.musicplayer.feature.player.PlayerViewModel
import com.musicplayer.feature.search.SearchScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主 Activity（@AndroidEntryPoint）。
 * 单 Activity + Compose 宿主（§1.1 / §6.8）。
 *
 * - topBar = TopAppBar(应用名) + TabRow（分段切换「在线搜索 / 本地音乐」），避免各屏重复头部（双头栏）；
 * - bottomBar = MiniPlayer（常驻迷你播放器）；
 * - content = when(tab) 渲染 SearchScreen / LocalScreen；
 * - PlayerScreen 以 AnimatedVisibility 覆盖层呈现（仅 currentSong!=null 时可展开）。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var isPlayerPageOpen by remember { mutableStateOf(false) }
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // 宿主统一头部：应用标题 + 分段切换（各 Screen 不再自带 TopAppBar，§6.8）
            Column {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) })
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("在线搜索") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("本地音乐") }
                    )
                }
            }
        },
        bottomBar = {
            MiniPlayer(
                viewModel = playerViewModel,
                onExpand = { if (playerUiState.currentSong != null) isPlayerPageOpen = true }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> SearchScreen(
                    onPlayQueue = { songs, index -> playerViewModel.play(songs, index) }
                )
                1 -> LocalScreen(
                    onPlayQueue = { songs, index -> playerViewModel.play(songs, index) },
                    onGoOnline = { selectedTab = 0 }
                )
            }

            // 全屏播放页覆盖层（从底部上滑，z 序最高；仅当前有歌曲时可见）
            AnimatedVisibility(
                visible = isPlayerPageOpen && playerUiState.currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                PlayerScreen(
                    viewModel = playerViewModel,
                    onCollapse = { isPlayerPageOpen = false }
                )
            }
        }
    }
}
