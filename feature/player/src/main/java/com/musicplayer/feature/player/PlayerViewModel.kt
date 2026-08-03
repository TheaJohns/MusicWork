package com.musicplayer.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.player.PlayerController
import com.musicplayer.core.domain.player.PlayerError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 播放 ViewModel（@HiltViewModel，注入 [PlayerController] 单例）。
 *
 * 通过 [combine] 聚合 PlayerController 的多个状态流为单一 [PlayerUiState]；
 * 一次性错误从 [PlayerController.errorEvent] 订阅后写入 [_error]，随状态一起下发。
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    // 订阅一次性错误事件，写入 _error 随状态流下发（修复：此前全文件未订阅，错误横幅恒不显示）
    init {
        viewModelScope.launch {
            playerController.errorEvent.collect { _error.value = it }
        }
    }

    private val _error = MutableStateFlow<PlayerError?>(null)

    val uiState: StateFlow<PlayerUiState> = combine(
        playerController.state,
        playerController.currentPosition,
        playerController.duration,
        playerController.currentSong,
        _error
    ) { state, position, duration, song, error ->
        PlayerUiState(
            currentSong = song,
            state = state,
            currentPositionMs = position,
            durationMs = duration,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    /** 设置播放队列并起播（列表项点击触发，US-3 / AC-3.3） */
    fun play(songs: List<Song>, startIndex: Int = 0) {
        playerController.setQueue(songs, startIndex)
    }
    fun onPlay() = playerController.play()
    fun onPause() = playerController.pause()
    fun onNext() = playerController.next()
    fun onPrev() = playerController.prev()
    fun onSeek(positionMs: Long) = playerController.seekTo(positionMs)

    /** 错误态重试：转发至 PlayerController.retry()（AC-7.2 兜底） */
    fun onRetry() = playerController.retry()

    /** 清除一次性错误（提示消费后调用） */
    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // 注意：PlayerController 为 @Singleton，跨页面共享，此处不调用 release()
        // release() 应在应用退出/Service 销毁时由持有方调用
    }
}
