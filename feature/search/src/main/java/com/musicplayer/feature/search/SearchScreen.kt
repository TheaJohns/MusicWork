package com.musicplayer.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.ui.components.EmptyView
import com.musicplayer.core.ui.components.ErrorView
import com.musicplayer.core.ui.components.LoadingView
import com.musicplayer.core.ui.components.SongRow

/**
 * 在线搜索屏幕（§2.2）。
 *
 * 仅渲染内容区（搜索框 + 列表），不含 TopAppBar（由宿主 MainActivity 统一持有，消除双头栏）。
 * 五态：初始提示 / Loading / 列表 / Empty / Error，严格对齐 [SearchUiState]。
 * 列表项点击经 [onPlayQueue] 回调交由宿主调用 PlayerController 起播。
 *
 * @param viewModel 搜索 ViewModel（默认 hiltViewModel）
 * @param onPlayQueue (songs, index) 点击某首歌时回传当前列表与索引，供宿主 setQueue + playAt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onPlayQueue: (List<Song>, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部固定内联搜索框（紧凑式，非展开 SearchBar，§2.2）
        SearchField(
            value = uiState.keyword,
            onValueChange = viewModel::onKeywordChange,
            onSearch = viewModel::onSearch,
            enabled = uiState.keyword.isNotBlank()
        )

        // 内容区：按状态渲染
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.error != null -> ErrorView(
                    message = uiState.error,
                    onRetry = viewModel::onSearch
                )
                uiState.isEmpty -> EmptyView(
                    icon = Icons.Filled.SearchOff,
                    message = "未找到相关歌曲",
                    actionLabel = null,
                    onAction = null
                )
                uiState.songs.isNotEmpty() -> {
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
                else -> EmptyView(
                    icon = Icons.Filled.Search,
                    message = "输入关键词搜索歌曲",
                    actionLabel = null,
                    onAction = null
                )
            }
        }
    }
}

/**
 * 内联搜索框：OutlinedTextField + 搜索 IconButton（§2.2 / §4.1）。
 * - 空关键词时搜索按钮禁用（AC-1.3）；
 * - 键盘 IME 回车（Search）触发搜索。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("搜索歌曲、歌手") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onSearch, enabled = enabled) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索"
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}
