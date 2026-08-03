package com.musicplayer.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 品牌种子色（Dynamic Color 不可用时回退，§0.1）
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFF6A1B9A)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFCE93D8)
)

/**
 * 应用全局主题（§0.1 / §6.1）。
 * - Light / Dark 双主题，跟随系统暗色模式；
 * - Android 12+ 优先启用 Dynamic Color（取系统壁纸色），低版本回退到品牌种子色；
 * - 全程使用 M3 色彩角色，业务代码仅引用角色，不硬编码色值。
 *
 * @param darkTheme 是否暗色主题（默认跟随系统）
 * @param dynamicColor 是否启用 Dynamic Color（默认开启，仅 API 31+ 生效）
 */
@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
