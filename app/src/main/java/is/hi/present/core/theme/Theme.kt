package `is`.hi.present.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SoftLavenderDark,
    onPrimary = White,

    secondary = BlushPink,
    onSecondary = TextPrimary,

    tertiary = MintAccent,
    onTertiary = TextPrimary,

    background = AppLavenderBackground,
    onBackground = TextPrimary,

    surface = SoftCard,
    onSurface = TextPrimary,

    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = OutlineSoft
)

@Composable
fun PresentTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}