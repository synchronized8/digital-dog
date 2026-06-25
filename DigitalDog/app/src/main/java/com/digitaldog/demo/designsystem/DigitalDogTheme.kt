package com.digitaldog.demo.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DogColors {
    val Background = Color(0xFFF7FAF7)
    val Surface = Color.White
    val SurfaceAlt = Color(0xFFEEF6F3)
    val TextPrimary = Color(0xFF24302F)
    val TextSecondary = Color(0xFF66736F)
    val PetWarm = Color(0xFFF2B6A0)
    val TechBlue = Color(0xFF4C8DFF)
    val Coral = Color(0xFFFF6F61)
    val SuccessGreen = Color(0xFF37A86B)
    val WarningYellow = Color(0xFFE7A93F)
    val WarningText = Color(0xFF7A4A00)
    val Border = Color(0xFFDDE7E3)
}

object DogSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 16.dp
    val Lg = 24.dp
    val Xl = 32.dp
    val TouchTarget = 48.dp
}

object DogTypography {
    val PageTitle = 24.sp
    val SectionTitle = 18.sp
    val PanelTitle = 16.sp
    val Body = 14.sp
    val Label = 12.sp
    val Button = 14.sp
    val DebugValue = 13.sp
}

object DogShape {
    val Panel = RoundedCornerShape(8.dp)
    val Button = RoundedCornerShape(8.dp)
}

private val DigitalDogColorScheme = lightColorScheme(
    background = DogColors.Background,
    surface = DogColors.Surface,
    primary = DogColors.TechBlue,
    secondary = DogColors.Coral,
    tertiary = DogColors.SuccessGreen,
    error = DogColors.WarningYellow,
    onBackground = DogColors.TextPrimary,
    onSurface = DogColors.TextPrimary,
    onPrimary = Color.White,
)

@Composable
fun DigitalDogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DigitalDogColorScheme,
        content = content,
    )
}
