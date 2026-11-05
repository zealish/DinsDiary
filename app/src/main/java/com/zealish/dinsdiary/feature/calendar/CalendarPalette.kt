package com.zealish.dinsdiary.feature.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.zealish.dinsdiary.ui.theme.LocalDarkTheme

/**
 * Mockup-exact calendar tints (`mockup.html` calendar screen day styles).
 * Light values are the mockup hexes; dark values keep the same hue
 * family readable on dark.
 */
data class CalendarPalette(
    val periodBg: Color,
    val periodText: Color,
    val predBg: Color,
    val predBorder: Color,
    val predText: Color,
    val fertileBg: Color,
    val fertileText: Color,
    val dayText: Color,
    val headText: Color,
    val mutedText: Color,
    val selectedBg: Color,
    val selectedText: Color,
    val todayOutline: Color,
)

private val LightCalendarPalette = CalendarPalette(
    periodBg = Color(0xFFF8DCE8),
    periodText = Color(0xFFA34F78),
    predBg = Color(0xFFFFF0F5),
    predBorder = Color(0xFFD98BAF),
    predText = Color(0xFFA65E81),
    fertileBg = Color(0xFFF0EAFA),
    fertileText = Color(0xFF8772AD),
    dayText = Color(0xFF5E4E5D),
    headText = Color(0xFFAA9BA6),
    mutedText = Color(0xFF8B7B89),
    selectedBg = Color(0xFFB96E92),
    selectedText = Color.White,
    todayOutline = Color(0xFFB96E92),
)

private val DarkCalendarPalette = CalendarPalette(
    periodBg = Color(0xFF5E3346),
    periodText = Color(0xFFF5C6D8),
    predBg = Color(0xFF3A2A33),
    predBorder = Color(0xFFE7A5BF),
    predText = Color(0xFFF5C6D8),
    fertileBg = Color(0xFF3B3350),
    fertileText = Color(0xFFD3C8EC),
    dayText = Color(0xFFE8DDE4),
    headText = Color(0xFF9A8B96),
    mutedText = Color(0xFF9A8B96),
    selectedBg = Color(0xFFB96E92),
    selectedText = Color.White,
    todayOutline = Color(0xFFE7A5BF),
)

val currentCalendarPalette: CalendarPalette
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current) DarkCalendarPalette else LightCalendarPalette
