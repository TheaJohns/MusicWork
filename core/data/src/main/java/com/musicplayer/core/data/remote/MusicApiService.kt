package com.musicplayer.core.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** 在线搜索响应 DTO（ARCH §3.2 / PRD §3.1.1） */
data class SearchMusicResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("result") val result: List<MusicItemDto>?
)

/** 单曲 DTO（ARCH §3.2） */
data class MusicItemDto(
    @SerializedName("author") val author: String?,  // 歌手
    @SerializedName("title") val title: String?,    // 歌名
    @SerializedName("pic") val pic: String?,        // 封面图 URL
    @SerializedName("type") val type: String?,      // 来源（如 netease）
    @SerializedName("url") val url: String?,        // 可播放外链 mp3（关键）
    @SerializedName("link") val link: String?,      // 原站链接
    @SerializedName("lrc") val lrc: String?,        // 歌词文本（可空）
    @SerializedName("songid") val songid: String?   // 歌曲 ID（接口可能返回 int/string，统一转 String）
)

/** Retrofit 接口（ARCH §3.2 / PRD §3.1.1） */
interface MusicApiService {
    @GET("searchMusic")
    suspend fun searchMusic(@Query("name") name: String): SearchMusicResponse
}
