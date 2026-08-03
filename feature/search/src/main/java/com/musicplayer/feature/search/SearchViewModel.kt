package com.musicplayer.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.player.PlayerController
import com.musicplayer.core.domain.usecase.SearchOnlineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 在线搜索 ViewModel（@HiltViewModel，注入 [SearchOnlineUseCase] 与 [PlayerController]）。
 * 暴露不可变 [uiState] 作为单一可信源。
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchOnlineUseCase: SearchOnlineUseCase,
    private val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** 搜索框输入变更（受控输入） */
    fun onKeywordChange(keyword: String) {
        _uiState.update { it.copy(keyword = keyword, error = null) }
    }

    /** 触发搜索（AC-1.3 空校验 / AC-1.4 trim 由 UseCase 处理） */
    fun onSearch() {
        val keyword = _uiState.value.keyword
        if (keyword.isBlank()) {
            // AC-1.3：空关键词提示「请输入关键词」，不发起请求
            _uiState.update { it.copy(error = "请输入关键词", isLoading = false) }
            return
        }
        search(keyword)
    }

    private fun search(keyword: String) {
        _uiState.update { it.copy(isLoading = true, error = null, isEmpty = false) }
        viewModelScope.launch {
            searchOnlineUseCase(keyword).collect { result ->
                result.fold(
                    onSuccess = { songs ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                songs = songs,
                                isEmpty = songs.isEmpty(),
                                error = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载失败",
                                songs = emptyList()
                            )
                        }
                    }
                )
            }
        }
    }

    /**
     * 列表项点击：将当前搜索结果设为播放队列并从该曲起播（US-3 / AC-3.3）。
     * [PlayerController.setQueue] 内部已从 startIndex 起播。
     */
    fun onSongClick(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOf(song).coerceAtLeast(0)
        playerController.setQueue(songs, index)
    }
}
