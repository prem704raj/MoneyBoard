package com.premraj.moneyboard.feature.dashboard.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.validation.EmojiValidator
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi

/**
 * Universal icon composable for expense items:
 * - Renders illustrated cropped drawables untinted with full fidelity for system presets.
 * - Renders native Unicode emojis for custom user items.
 * - Gracefully falls back to vector icons.
 */
@Composable
fun ExpenseIconView(
    item: ExpenseRowUi,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    // Resolve drawable: use item property if present, or look up via CategoryIconMapper
    val drawableRes = item.iconDrawableRes ?: CategoryIconMapper.getPresetDrawable(item.categoryId)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when {
            drawableRes != null -> {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            !item.iconEmoji.isNullOrBlank() -> {
                val cleanEmoji = item.iconEmoji.substringBefore("|").trim()
                Text(
                    text = cleanEmoji,
                    fontSize = (size.value * 0.75f).sp,
                    textAlign = TextAlign.Center
                )
            }
            EmojiValidator.isValidEmoji(item.title) -> {
                Text(
                    text = item.title,
                    fontSize = (size.value * 0.75f).sp,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.badgeColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Direct overload for rendering an icon by individual properties.
 */
@Composable
fun ExpenseIconView(
    @DrawableRes iconDrawableRes: Int? = null,
    iconEmoji: String? = null,
    iconVector: ImageVector? = null,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when {
            iconDrawableRes != null -> {
                Image(
                    painter = painterResource(id = iconDrawableRes),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                )
            }
            !iconEmoji.isNullOrBlank() -> {
                Text(
                    text = iconEmoji,
                    fontSize = (size.value * 0.75f).sp,
                    textAlign = TextAlign.Center
                )
            }
            iconVector != null -> {
                Icon(
                    imageVector = iconVector,
                    contentDescription = contentDescription,
                    tint = if (tint != Color.Unspecified) tint else Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
