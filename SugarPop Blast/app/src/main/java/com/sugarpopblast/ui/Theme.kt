package com.sugarpopblast.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object CandyColors {
    val ink = Color(0xFF4A1B6D)
    val inkSoft = Color(0xFF8A5BA8)
    val cream = Color(0xFFFFF7EC)
    val creamShade = Color(0xFFFDE8D0)
    val pink = Color(0xFFFF5E9C)
    val pinkDeep = Color(0xFFD81B60)
    val pinkSoft = Color(0xFFFFC1DA)
    val gold = Color(0xFFFFC93C)
    val goldDeep = Color(0xFFE39B00)
    val mint = Color(0xFF39C97A)
    val boardBlue = Color(0xFF2F3F92)
    val text = Color(0xFFFFFFFF)
    val muted = Color(0xE6FFFFFF)
}

/** Candy valley: sky fading into green hills. */
val CandyBackground = Brush.verticalGradient(
    listOf(
        Color(0xFF6FD2F2),
        Color(0xFF9FE6E0),
        Color(0xFFA8E07A),
        Color(0xFF6FC45F)
    )
)

val GoldGradient = Brush.verticalGradient(listOf(Color(0xFFFFE082), Color(0xFFFFB300)))
val PinkGradient = Brush.verticalGradient(listOf(Color(0xFFFF7FB2), Color(0xFFFF3D7F)))
val PurpleGradient = Brush.verticalGradient(listOf(Color(0xFFB06BFF), Color(0xFF7B2FF7)))
val GreenGradient = Brush.verticalGradient(listOf(Color(0xFF7BE495), Color(0xFF2FA84F)))

/**
 * White headings sit on a pale sky (and sometimes a white cloud), so they need
 * a soft dark shadow to stay legible.
 */
val TitleShadow = Shadow(
    color = Color(0x66000000),
    offset = Offset(0f, 1.5f),
    blurRadius = 5f
)

private val CandyScheme = lightColorScheme(
    primary = CandyColors.pink,
    onPrimary = Color.White,
    secondary = CandyColors.gold,
    onSecondary = CandyColors.ink,
    background = Color(0xFF9FE6E0),
    onBackground = CandyColors.ink,
    surface = CandyColors.cream,
    onSurface = CandyColors.ink,
    surfaceVariant = CandyColors.creamShade,
    onSurfaceVariant = CandyColors.ink,
    outline = CandyColors.pinkSoft
)

private val CandyTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 0.6.sp)
)

@Composable
fun CandyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CandyScheme,
        typography = CandyTypography,
        content = content
    )
}

