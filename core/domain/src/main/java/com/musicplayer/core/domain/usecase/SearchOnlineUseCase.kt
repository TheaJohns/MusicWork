package com.musicplayer.core.domain.usecase

import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.repository.MusicRepository
import kotlin.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 在线搜索用例（ARCH §3.5）：包裹 [MusicRepository.searchOnline]。
 *
 * 业务规则（AC-1.3 / AC-1.4）：
 * - 对关键词做 trim 去前后空格；
 * - 空/空白关键词交给下游处理（ViewModel 侧做空校验与提示）。
 */
class SearchOnlineUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(keyword: String): Flow<Result<List<Song>>> {
        val trimmed = keyword.trim()
        return repository.searchOnline(trimmed)
    }
}
