package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.feature.dashboard.formatIndianRupees

@Composable
fun TotalAllocationBanner(
    amountText: String = "₹69,350",
    remainingText: String? = null,
    salaryText: String? = null,
    modifier: Modifier = Modifier
) {
    val totalClean = amountText.replace("₹", "").replace(",", "").trim().toLongOrNull() ?: 0L
    val salaryClean = salaryText?.replace("/ month", "")?.replace("₹", "")?.replace(",", "")?.trim()?.toLongOrNull()
    val remainingClean = if (salaryClean != null) salaryClean - totalClean else null
    val isPositive = remainingClean == null || remainingClean >= 0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clean Total Monthly Expenses Bar (transparent, background coloring removed)
        Surface(
            modifier = Modifier.weight(if (remainingClean != null) 1.55f else 1f),
            shape = RoundedCornerShape(10.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (amountText.startsWith("₹")) amountText else "₹$amountText",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Fresh Colourful You Save / Over Budget Card
        if (remainingClean != null) {
            Surface(
                modifier = Modifier.weight(0.85f),
                shape = RoundedCornerShape(10.dp),
                color = if (isPositive) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, if (isPositive) Color(0xFFA7F3D0) else Color(0xFFFECACA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 9.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                if (isPositive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Rounded.Spa else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = if (isPositive) Color(0xFF059669) else Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isPositive) "You save" else "Over budget",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isPositive) Color(0xFF059669) else Color(0xFFDC2626),
                            maxLines = 1,
                            softWrap = false
                        )
                        val remFormatted = formatIndianRupees(
                            if (isPositive) remainingClean else -remainingClean
                        )
                        Text(
                            text = "₹$remFormatted",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isPositive) Color(0xFF047857) else Color(0xFFB91C1C),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
