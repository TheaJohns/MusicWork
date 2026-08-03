package com.musicplayer.core.ui.utils

/**
 * 时长格式化工具（UI 展示用，§6 进度文本 mm:ss）。
 */

/**
 * 将毫秒时长格式化为 mm:ss（超过 60 分钟仅取低两位分钟，符合 MVP 进度展示）。
 * @param durationMs 时长(ms)，<=0 返回 00:00
 */
fun formatMMSS(durationMs: Long): String {
    if (durationMs <= 0) return "00:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
