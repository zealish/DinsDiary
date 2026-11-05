package com.zealish.dinsdiary.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.zealish.dinsdiary.R

/**
 * Lora: Georgia-like transitional serif for display headings.
 * The mockup uses Georgia (unavailable on Android); Lora is the
 * closest freely-bundled match. Body text keeps the system sans.
 */
val DinsSerif = FontFamily(
    Font(R.font.lora_medium, FontWeight.Medium),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold),
)
