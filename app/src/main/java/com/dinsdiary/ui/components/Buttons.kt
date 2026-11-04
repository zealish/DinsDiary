package com.dinsdiary.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinsdiary.ui.theme.currentHomePalette

/**
 * Mockup `.btn` styles (`mockup.html`) shared across screens.
 * Sizes map 1px → 1sp/1dp, matching the existing home-screen treatment.
 */

/** Filled pink primary action (mockup `.btn`). */
@Composable
fun DinsPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD98BAF),
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 12.dp),
        content = { content() },
    )
}

/** Soft pink secondary action (mockup `.btn.secondary`). */
@Composable
fun DinsSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = currentHomePalette
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.secondaryBg,
            contentColor = palette.secondaryText,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.secondaryBorder),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 12.dp),
        content = { content() },
    )
}

/** Borderless plum text action (mockup `.btn.ghost`). */
@Composable
fun DinsGhostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = currentHomePalette.eyebrow),
        content = { content() },
    )
}

/** Convenience overload for plain 12sp extrabold labels. */
@Composable
fun DinsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DinsPrimaryButton(onClick = onClick, modifier = modifier) {
        androidx.compose.material3.Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** Convenience overload for plain 12sp extrabold labels. */
@Composable
fun DinsSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DinsSecondaryButton(onClick = onClick, modifier = modifier) {
        androidx.compose.material3.Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** Convenience overload for plain 12sp labels. */
@Composable
fun DinsGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DinsGhostButton(onClick = onClick, modifier = modifier) {
        androidx.compose.material3.Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
