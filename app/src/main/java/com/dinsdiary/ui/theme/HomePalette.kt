package com.dinsdiary.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** App-resolved theme (user can override system); drives icon contrast and home tints. */
val LocalDarkTheme = compositionLocalOf { false }

/**
 * Mockup-exact home tints (mockup.html home screen). Light values are the
 * mockup hexes; dark values keep the same hue family readable on dark.
 */
data class HomePalette(
    val softTop: Color,
    val softBottom: Color,
    val cardBorder: Color,
    val pillBg: Color,
    val pillText: Color,
    val secondaryBg: Color,
    val secondaryText: Color,
    val secondaryBorder: Color,
    val statBg: Color,
    val statBorder: Color,
    val choiceBg: Color,
    val choiceBorder: Color,
    val choiceText: Color,
    val trackBg: Color,
    val progStart: Color,
    val progEnd: Color,
    val roundBg: Color,
    val roundIcon: Color,
    val eyebrow: Color,
    val lavBg: Color,
    val lavText: Color,
    val noteBg: Color,
    val noteBorder: Color,
    val noteText: Color,
)

private val LightHomePalette = HomePalette(
    softTop = Color(0xFFFFF1F6),
    softBottom = Color(0xFFFBF5FF),
    cardBorder = Color(0xFFF0E4EB),
    pillBg = Color(0xFFFCECF3),
    pillText = Color(0xFFA45F7F),
    secondaryBg = Color(0xFFFFF0F5),
    secondaryText = Color(0xFFA65E81),
    secondaryBorder = Color(0xFFF5DCE7),
    statBg = Color(0xFFFFF7FA),
    statBorder = Color(0xFFF7E9EF),
    choiceBg = Color(0xFFFFFFFF),
    choiceBorder = Color(0xFFEEE0E8),
    choiceText = Color(0xFF7E6D7B),
    trackBg = Color(0xFFF5EAF0),
    progStart = Color(0xFFE7A5BF),
    progEnd = Color(0xFFC39AD9),
    roundBg = Color(0xFFFFF2F6),
    roundIcon = Color(0xFFA65E81),
    eyebrow = Color(0xFFB96E92),
    lavBg = Color(0xFFF1EDFA),
    lavText = Color(0xFF8873B1),
    noteBg = Color(0xFFFFF8E9),
    noteBorder = Color(0xFFF7EBCF),
    noteText = Color(0xFF8C7548),
)

private val DarkHomePalette = HomePalette(
    softTop = Color(0xFF43303C),
    softBottom = Color(0xFF33262E),
    cardBorder = Color(0xFF4E3B47),
    pillBg = Color(0xFF4E2F40),
    pillText = Color(0xFFF5C6D8),
    secondaryBg = Color(0xFF3A2A33),
    secondaryText = Color(0xFFF5C6D8),
    secondaryBorder = Color(0xFF5E4250),
    statBg = Color(0xFF2E2128),
    statBorder = Color(0xFF4E3B47),
    choiceBg = Color(0xFF2E2128),
    choiceBorder = Color(0xFF5E4250),
    choiceText = Color(0xFFDCC3CF),
    trackBg = Color(0xFF453038),
    progStart = Color(0xFFE7A5BF),
    progEnd = Color(0xFFC39AD9),
    roundBg = Color(0xFF3A2A33),
    roundIcon = Color(0xFFF5C6D8),
    eyebrow = Color(0xFFE7A5BF),
    lavBg = Color(0xFF3B324B),
    lavText = Color(0xFFC9BBE8),
    noteBg = Color(0xFF3A3226),
    noteBorder = Color(0xFF5A4E33),
    noteText = Color(0xFFD8C48F),
)

val currentHomePalette: HomePalette
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current) DarkHomePalette else LightHomePalette
