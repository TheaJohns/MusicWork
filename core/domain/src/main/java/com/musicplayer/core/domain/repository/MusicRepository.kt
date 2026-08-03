package com.musicplayer.core.domain.repository

import com.musicplayer.core.domain.model.Song
import kotlin.Result
import kotlinx.coroutines.flow.Flow

/**
 * 音乐仓库接口（ARCH §3.5），由 data 层实现。
 *
 * 职责边界：Repository 仅暴露「列表数据」（[List]<[Song]>）；
 * 播放状态由 [com.musicplayer.core.domain.player.PlayerController] 独立暴露，
 * 二者解耦，列表与播放栏可各自订阅。
 */
interface MusicRepository {
    /**
     * 在线搜索：返回带加载/错误态的流。
     * 成功判定 code == 200，否则包装为失败（承载 message，支撑 US-7）。
     */
    fun searchOnline(keyword: String): Flow<Result<List<Song>>>

    /** 本地扫描：返回带加载/错误态的流 */
    fun getLocalSongs(): Flow<Result<List<Song>>>
}
