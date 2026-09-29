package app.sohdcode.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BB6D0),
    onPrimary = Color(0xFF0F1419),
    background = Color(0xFF0F1419),
    surface = Color(0xFF171E26),
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF9AA8B5),
    outline = Color(0xFF2A3440),
    error = Color(0xFFD67A7A)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3D6B99),
    onPrimary = Color.White,
    background = Color(0xFFF4F6F8),
    surface = Color.White,
    onBackground = Color(0xFF14202B),
    onSurface = Color(0xFF14202B),
    onSurfaceVariant = Color(0xFF516070),
    outline = Color(0xFFD0D7DE),
    error = Color(0xFFB42318)
)

@Composable
fun SohdCodeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(
            headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 30.sp),
            bodySmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        ),
        content = content
    )
}
