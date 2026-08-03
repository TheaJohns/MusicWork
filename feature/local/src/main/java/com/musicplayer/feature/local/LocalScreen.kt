package com.musicplayer.feature.local

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.ui.components.EmptyView
import com.musicplayer.core.ui.components.ErrorView
import com.musicplayer.core.ui.components.LoadingView
import com.musicplayer.core.ui.components.SongRow

/**
 * 本地音乐屏幕（§2.3）。
 *
 * 仅渲染内容区（权限分支 + 列表），不含 TopAppBar（由宿主 MainActivity 统一持有）。
 * 依据 [LocalUiState.permissionState] 分支：
 * - UNDETERMINED：进入即触发系统权限请求（LaunchedEffect），UI 显示轻量提示；
 * - DENIED：显示授予权限卡片，点击再次请求；
 * - DENIED_PERMANENTLY：显示去设置引导卡片，跳系统应用设置页；
 * - GRANTED：按 Loading / Error / Empty / 列表 渲染。
 *
 * @param viewModel 本地 ViewModel（默认 hiltViewModel）
 * @param onPlayQueue (songs, index) 点击某首歌时回传当前列表与索引
 * @param onGoOnline 空库时「去在线听歌」→ 切回在线 Tab（AC-6.2）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalScreen(
    viewModel: LocalViewModel = hiltViewModel(),
    onPlayQueue: (List<Song>, Int) -> Unit,
    onGoOnline: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 按 API 分档选择权限（§2.5 / ARCH §4.2）
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // 系统权限请求启动器（结果回写 ViewModel）
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.onPermissionResult(true)
            } else {
                // 拒绝后判断是否为「不再询问」：无法再次弹窗 → DENIED_PERMANENTLY
                val activity = context as? androidx.activity.ComponentActivity
                val showRationale = activity?.shouldShowRequestPermissionRationale(permission) ?: false
                if (showRationale) viewModel.onPermissionResult(false)
                else viewModel.onPermissionDeniedPermanently()
            }
        }
    )

    // 进入本地 Tab 且尚未定夺时自动请求权限（§2.5）
    LaunchedEffect(Unit) {
        if (uiState.permissionState == PermissionState.UNDETERMINED) {
            permissionLauncher.launch(permission)
        }
    }

    when (uiState.permissionState) {
        PermissionState.UNDETERMINED -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingView(message = "正在请求存储权限…")
            }
        }
        PermissionState.DENIED -> {
            EmptyView(
                icon = Icons.Filled.Lock,
                message = "需要存储权限以读取本地音乐",
                actionLabel = "授予权限",
                onAction = { permissionLauncher.launch(permission) }
            )
        }
        PermissionState.DENIED_PERMANENTLY -> {
            EmptyView(
                icon = Icons.Filled.Settings,
                message = "已拒绝且不再询问，请前往系统设置开启",
                actionLabel = "去设置",
                onAction = {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }
            )
        }
        PermissionState.GRANTED -> {
            when {
                uiState.isLoading -> LoadingView()
                uiState.error != null -> ErrorView(
                    message = uiState.error,
                    onRetry = viewModel::loadSongs
                )
                uiState.songs.isEmpty() -> EmptyView(
                    icon = Icons.Filled.MusicOff,
                    message = "设备暂无可播放的音乐",
                    actionLabel = "去在线听歌",
                    onAction = onGoOnline
                )
                else -> {
                    val songs = uiState.songs
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(songs, key = { it.id }) { song ->
                            SongRow(
                                song = song,
                                onClick = {
                                    val index = songs.indexOf(song)
                                    onPlayQueue(songs, index)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
