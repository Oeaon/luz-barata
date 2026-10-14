package com.luzbarata.app.ui.theme

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
import com.luzbarata.core.PriceLevel

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E6B3A),
    secondary = Color(0xFF52634F),
    tertiary = Color(0xFF8A5100),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD79B),
    secondary = Color(0xFFB9CCB4),
    tertiary = Color(0xFFFFB86E),
)

@Composable
fun LuzBarataTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

/** Verde / ámbar / rojo según el tercio del día. */
@Composable
fun levelColor(level: PriceLevel): Color {
    val dark = isSystemInDarkTheme()
    return when (level) {
        PriceLevel.LOW -> if (dark) Color(0xFF81C784) else Color(0xFF2E7D32)
        PriceLevel.MEDIUM -> if (dark) Color(0xFFFFD54F) else Color(0xFFE09B00)
        PriceLevel.HIGH -> if (dark) Color(0xFFE57373) else Color(0xFFC62828)
    }
}
