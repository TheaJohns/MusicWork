package com.musicplayer.core.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.musicplayer.core.domain.model.Song
import com.musicplayer.core.domain.model.SongSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 本地音乐数据源（ARCH §3.3）：基于 [MediaStore] 查询外置存储音频。
 * 关键策略：
 * - 过滤：仅音频（IS_MUSIC=1 或 audio / * MIME）、时长 > 30s（剔除录音/片段）、按 DATA 去重；
 * - 播放/封面统一使用 content URI（[ContentUris]），绝不依赖 DATA 文件路径（R5 分区存储）；
 * - 在 [Dispatchers.IO] 执行，首屏上限 200 条（R3 性能）；
 * - albumId 有效时用专辑封面 content URI 作为 coverUrl。
 */
class LocalMusicDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val albumArtUri = Uri.parse("content://media/external/audio/albumart")

    suspend fun getLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.MIME_TYPE
        )
        // 仅音频 + 时长 > 30s（在 SQL 层过滤，减少内存与解析开销）
        val selection = buildString {
            append("(${MediaStore.Audio.Media.IS_MUSIC} = ? OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE ?)")
            append(" AND ${MediaStore.Audio.Media.DURATION} > ?")
        }
        val selectionArgs = arrayOf("1", "audio/%", 30_000.toString())
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val seenData = LinkedHashSet<String>()
        val songs = mutableListOf<Song>()

        context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val isMusicCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val data = cursor.getString(dataCol) ?: continue
                    // 按 DATA 去重（同文件多条记录）
                    if (!seenData.add(data)) continue

                    val isMusic = cursor.getInt(isMusicCol) == 1
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                    // 二次保险：既非 IS_MUSIC 也非 audio/* 则跳过
                    if (!isMusic && mime != null && !mime.startsWith("audio/")) continue

                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)

                    val albumId = cursor.getLong(albumIdCol)
                    val coverUri = if (albumId > 0) {
                        ContentUris.withAppendedId(albumArtUri, albumId).toString()
                    } else null

                    songs += Song(
                        id = id.toString(),
                        title = cursor.getString(titleCol) ?: "未知歌曲",
                        artist = cursor.getString(artistCol) ?: "未知歌手",
                        album = cursor.getString(albumCol),
                        coverUrl = coverUri,
                        durationMs = cursor.getLong(durationCol),
                        source = SongSource.LOCAL,
                        playUrl = contentUri.toString(), // 统一 content URI 播放（R5）
                        lyric = null,
                        originLink = null
                    )
                    // 首屏上限 200 条（R3），后续滚动加载更多由 UI/分页扩展
                    if (songs.size >= 200) break
                }
            }
        songs
    }
}
