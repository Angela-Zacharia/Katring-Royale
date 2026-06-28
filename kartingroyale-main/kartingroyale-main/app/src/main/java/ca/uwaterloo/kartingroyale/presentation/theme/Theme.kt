package ca.uwaterloo.kartingroyale.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = darkColorScheme(
    primary = RacingTeal,
    onPrimary = Color.Black,
    primaryContainer = RacingTealVariant,
    onPrimaryContainer = RacingTeal,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = RedFlag,
    onTertiary = Color.White,
    background = Asphalt,
    onBackground = ChromeWhite,
    surface = AsphaltLight,
    onSurface = ChromeWhite,
    surfaceVariant = Pitlane,
    onSurfaceVariant = SilverGrey,
    outline = CheckeredGrey,
    error = RedFlag,
)

@Composable
fun KartingroyaleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}