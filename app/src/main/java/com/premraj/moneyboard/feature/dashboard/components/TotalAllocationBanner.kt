package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

@Composable
fun TotalAllocationBanner(
    modifier: Modifier = Modifier,
    amountText: String = "₹69,350"
) {
    val gradient = Brush.horizontalGradient(
        listOf(
            MoneyBoardColors.Navy900,
            MoneyBoardColors.Blue700
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = gradient,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Calculate,
            contentDescription = null,
            tint = Color.White
        )

        Text(
            text = "  Total Monthly Allocation",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = amountText,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
