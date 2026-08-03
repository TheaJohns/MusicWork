package com.musicplayer.core.domain.usecase

import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.repository.MusicRepository
import kotlin.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取本地歌曲用例（ARCH §3.5）：包裹 [MusicRepository.getLocalSongs]。
 * 本地扫描的权限判定与列表展示由 ViewModel / UI 层驱动。
 */
class GetLocalSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<Result<List<Song>>> = repository.getLocalSongs()
}
