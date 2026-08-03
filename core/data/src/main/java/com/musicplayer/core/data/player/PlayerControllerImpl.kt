package com.musicplayer.core.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.player.PlaybackQueue
import com.musicplayer.core.domain.player.PlayerController
import com.musicplayer.core.domain.player.PlayerError
import com.musicplayer.core.domain.player.PlayerErrorType
import com.musicplayer.core.domain.player.PlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 播放控制器实现（ARCH §3.4 / §1.3）：基于 AndroidX Media3 ExoPlayer。
 *
 * 设计要点：
 * - 在线外链与本地 content URI 统一用 [MediaItem.fromUri] 收口为同一抽象；
 * - 状态/进度/错误暴露为 [StateFlow]/[SharedFlow]，列表与播放栏各自订阅；
 * - 进度按 ~500ms 节流回发（US-5）；
 * - 播完（STATE_ENDED）自动下一首，尾曲播完 → IDLE 停止（D3 / R7）；
 * - onError 统一转 [PlayerError] 经 [errorEvent] 抛出，禁止崩溃（US-7 / NFR 稳定性）；
 * - 队列头/尾保持不循环（D3 / R6）。
 */
@Singleton
class PlayerControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PlayerController {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val _state = MutableStateFlow<PlayerState>(PlayerState.IDLE)
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    // extraBufferCapacity=1：确保最新错误不丢失（one-shot 事件）
    private val _errorEvent = MutableSharedFlow<PlayerError>(extraBufferCapacity = 1)
    override val errorEvent: SharedFlow<PlayerError> = _errorEvent.asSharedFlow()

    private var queue: PlaybackQueue = PlaybackQueue(emptyList(), -1)

    // 控制器为 @Singleton，跟随应用生命周期；进度轮询在主线程（避免跨线程访问 ExoPlayer）
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        // 就绪后读取真实时长（在线歌曲 durationMs 初始为 null）
                        _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                    }
                    Player.STATE_ENDED -> {
                        // 播完自动下一首；尾曲 → IDLE 停止（D3 / R7）
                        val isLast = queue.currentIndex >= 0 &&
                            queue.currentIndex >= queue.songs.lastIndex
                        if (isLast) {
                            stopProgressLoop()
                            exoPlayer.pause()
                            _state.value = PlayerState.IDLE
                        } else {
                            next()
                        }
                    }
                    else -> { /* STATE_IDLE / STATE_BUFFERING 不处理 */ }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // 错误态 / 停止态(IDLE)不被 isPlaying 回调覆盖，避免状态回退
                if (_state.value == PlayerState.ERROR || _state.value == PlayerState.IDLE) return
                _state.value = if (isPlaying) PlayerState.PLAYING else PlayerState.PAUSED
                if (isPlaying) startProgressLoop() else stopProgressLoop()
            }

            override fun onPlayerError(error: PlaybackException) {
                handleError(error)
            }
        })
    }

    override fun setQueue(songs: List<Song>, startIndex: Int) {
        val safeStart = if (songs.isEmpty()) -1 else startIndex.coerceIn(0, songs.lastIndex)
        queue = PlaybackQueue(songs, safeStart)
        if (songs.isNotEmpty() && safeStart in songs.indices) {
            playAt(safeStart)
        }
    }

    override fun playAt(index: Int) {
        if (index !in queue.songs.indices) return
        queue = queue.copy(currentIndex = index)
        val song = queue.current() ?: return
        _currentSong.value = song
        _currentPosition.value = 0L
        _duration.value = song.durationMs ?: 0L
        // 在线外链与本地 content URI 统一收口（R5 / §1.3）
        exoPlayer.setMediaItem(MediaItem.fromUri(song.playUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _state.value = PlayerState.PLAYING
    }

    override fun play() {
        if (queue.current() == null) return
        exoPlayer.playWhenReady = true
        _state.value = PlayerState.PLAYING
    }

    override fun pause() {
        pauseInternal()
    }

    override fun next() {
        // 队列尾保持尾曲（D3）
        val nextIdx = queue.nextIndex()
        if (nextIdx == queue.currentIndex) return
        playAt(nextIdx)
    }

    override fun prev() {
        // 队列头保持头曲（D3）
        val prevIdx = queue.prevIndex()
        if (prevIdx == queue.currentIndex) return
        playAt(prevIdx)
    }

    override fun retry() {
        // 以当前索引重建 MediaItem + prepare + playWhenReady，错误态也能重播（AC-7.2）
        val idx = queue.currentIndex
        if (idx in queue.songs.indices) playAt(idx)
    }

    override fun seekTo(positionMs: Long) {
        val pos = positionMs.coerceAtLeast(0L)
        exoPlayer.seekTo(pos)
        _currentPosition.value = pos
    }

    override fun release() {
        stopProgressLoop()
        exoPlayer.release()
        queue = PlaybackQueue(emptyList(), -1)
        _state.value = PlayerState.IDLE
        _currentSong.value = null
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    // ====================== 私有辅助 ======================

    private fun pauseInternal() {
        exoPlayer.playWhenReady = false
        _state.value = PlayerState.PAUSED
        stopProgressLoop()
    }

    /** 进度轮询：每 ~500ms 回发一次（US-5 节流，避免过频） */
    private fun startProgressLoop() {
        stopProgressLoop()
        progressJob = scope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                delay(500L)
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }

    /** 将 ExoPlayer 错误映射为领域错误并抛事件（US-7 兜底） */
    private fun handleError(error: PlaybackException) {
        val song = queue.current()
        val type = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                PlayerErrorType.NETWORK
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                PlayerErrorType.SOURCE_INVALID
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED ->
                PlayerErrorType.DECODE
            else -> PlayerErrorType.UNKNOWN
        }
        _state.value = PlayerState.ERROR
        _errorEvent.tryEmit(PlayerError(song, type, error.message))
    }
}
