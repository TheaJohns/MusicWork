package com.musicplayer.feature.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.player.PlayerController
import com.musicplayer.core.domain.usecase.GetLocalSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 本地音乐 ViewModel（@HiltViewModel，注入 [GetLocalSongsUseCase] 与 [PlayerController]）。
 *
 * 权限状态由 UI / 系统回调驱动（见 onPermissionResult / onPermissionDeniedPermanently）；
 * 后续阶段接入 [com.musicplayer.core.data.store.PermissionDataStore] 持久化「不再询问」。
 */
@HiltViewModel
class LocalViewModel @Inject constructor(
    private val getLocalSongsUseCase: GetLocalSongsUseCase,
    private val playerController: PlayerController
    // TODO: 注入 PermissionDataStore 以持久化「不再询问」标记（Sprint 2 / US-6）
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalUiState())
    val uiState: StateFlow<LocalUiState> = _uiState.asStateFlow()

    /** 系统权限回调：授权 → 扫描；拒绝 → DENIED */
    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _uiState.update { it.copy(permissionState = PermissionState.GRANTED) }
            loadSongs()
        } else {
            _uiState.update { it.copy(permissionState = PermissionState.DENIED) }
        }
    }

    /** 拒绝且「不再询问」：置为 DENIED_PERMANENTLY 并持久化标记（AC-6.2） */
    fun onPermissionDeniedPermanently() {
        _uiState.update { it.copy(permissionState = PermissionState.DENIED_PERMANENTLY) }
        // TODO: viewModelScope.launch { permissionDataStore.setNeverAskAgain(true) }
    }

    /** 异步扫描本地音乐（Dispatchers.IO 由 LocalMusicDataSource 内部保证） */
    fun loadSongs() {
        _uiState.update {
            it.copy(isLoading = true, error = null, permissionState = PermissionState.GRANTED)
        }
        viewModelScope.launch {
            getLocalSongsUseCase().collect { result ->
                result.fold(
                    onSuccess = { songs ->
                        _uiState.update {
                            it.copy(isLoading = false, songs = songs, error = null)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isLoading = false, error = e.message ?: "扫描失败")
                        }
                    }
                )
            }
        }
    }

    /**
     * 列表项点击：将当前本地列表设为播放队列并从该曲起播（US-3 / AC-3.3）。
     * [PlayerController.setQueue] 内部已从 startIndex 起播。
     */
    fun onSongClick(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOf(song).coerceAtLeast(0)
        playerController.setQueue(songs, index)
    }
}
