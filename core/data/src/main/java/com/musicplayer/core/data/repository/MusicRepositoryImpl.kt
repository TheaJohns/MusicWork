package com.musicplayer.core.data.repository

import com.musicplayer.core.data.local.LocalMusicDataSource
import com.musicplayer.core.data.mapper.MusicResultMapper
import com.musicplayer.core.data.remote.MusicApiService
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.repository.MusicRepository
import kotlin.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository 实现（ARCH §3.5）：聚合在线 API 与本地数据源，统一包装为 [Result] 流。
 */
class MusicRepositoryImpl @Inject constructor(
    private val apiService: MusicApiService,
    private val localDataSource: LocalMusicDataSource
) : MusicRepository {

    override fun searchOnline(keyword: String): Flow<Result<List<Song>>> = flow {
        // runCatching 统一捕获网络/解析/业务异常 → 包装为 Result
        emit(
            runCatching {
                val resp = apiService.searchMusic(keyword)
                // 成功判定：code == 200（PRD §3.1.1 / US-7）
                if (resp.code != 200) {
                    throw ApiException(resp.code, resp.message ?: "加载失败")
                }
                // 过滤无 url 条目 + 去重 + 缺字段占位（R1/R2）
                MusicResultMapper.map(resp.result ?: emptyList())
            }
        )
    }

    override fun getLocalSongs(): Flow<Result<List<Song>>> = flow {
        emit(runCatching { localDataSource.getLocalSongs() })
    }
}

/** 在线接口业务异常（code != 200 时抛出，承载 message 供 UI 展示） */
class ApiException(val code: Int, override val message: String) : Exception(message)
