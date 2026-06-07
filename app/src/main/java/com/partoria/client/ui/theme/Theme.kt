package com.partoria.client.ui.theme

import android.app.Activity
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
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

object AppColors {
    val Primary = Color(0xFF6C63FF)
    val PrimaryDark = Color(0xFF5A52D5)
    val Error = Color(0xFFFF6B6B)
    val Success = Color(0xFF4CAF50)

    val BackgroundStart = Color(0xFF1A1A2E)
    val BackgroundEnd = Color(0xFF16213E)
    val SurfaceLight = Color.White.copy(alpha = 0.1f)
    val SurfaceDark = Color.White.copy(alpha = 0.05f)

    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.7f)
    val TextDarkPrimary = Color(0xFF1A1A2E)
    val TextDarkSecondary = Color.Gray
    val TextDarkHint = Color.Gray.copy(alpha = 0.7f)
    val TextHint = Color.White.copy(alpha = 0.5f)
    val TextError = Color(0xFFFF6B6B)

    val BorderFocused = Color(0xFF6C63FF)
    val BorderUnfocused = Color.White.copy(alpha = 0.3f)

    val CardWhite = Color.White.copy(alpha = 0.95f)
    val CardBackground = Color.White.copy(alpha = 0.1f)
    val ButtonBackground = Color(0xFF6C63FF)
    val DeleteButtonBackground = Color(0xFFFF6B6B).copy(alpha = 0.1f)
    val EditButtonBackground = Color(0xFF6C63FF).copy(alpha = 0.1f)
}

object AppDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 12.dp
    val PaddingLarge = 16.dp
    val PaddingXLarge = 20.dp
    val PaddingXXLarge = 32.dp

    val PaddingMicro = 4.dp
    val PaddingCard = 16.dp
    val PaddingScreen = 16.dp
    val PaddingButton = 16.dp

    val BottomNavPaddingLarge = 90.dp
    val TopContentPadding = 6.dp
    val CardWidthEmptyState = 280.dp
    val CardWidthErrorState = 280.dp
    val SnackbarBottomOffset = 80.dp
    val CardWidthPartNotFound = 192.dp

    val ButtonHeight = 52.dp
    val IconSizeSmall = 16.dp
    val IconSizeMedium = 20.dp
    val IconSizeNormal = 24.dp
    val IconSizeLarge = 32.dp
    val IconSizeXLarge = 64.dp
    val IconSizeXXLarge = 80.dp
    val IconSizeAvatar = 120.dp

    val AvatarSize = 120.dp
    val AvatarIconSize = 80.dp
    val FabSize = 40.dp
    val IconButtonSize = 36.dp
    val AddButtonSize = 56.dp

    val CornerRadiusSmall = 12.dp
    val CornerRadiusMedium = 16.dp
    val CornerRadiusLarge = 20.dp
    val CornerRadiusXLarge = 24.dp
    val CornerRadiusCard = 16.dp
    val CornerRadiusButton = 12.dp
    val CornerRadiusAvatar = 32.dp

    val ElevationDefault = 4.dp

    val SpacingLarge = 64.dp

    val CardIconBoxSize = 60.dp
    val CardFavoriteButtonSize = 44.dp
}


@Composable
fun PartoriaClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
        typography = Typography,
        content = content
    )
}