package com.premraj.moneyboard.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

@Composable
fun CompetitorComparisonDialog(
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
        titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
        textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MoneyBoardColors.Orange700,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Why Choose SalaryLane?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SalaryLane is built to eliminate clutter, protect user privacy, and make monthly personal budgeting transparent and effortless.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                // Comparison Cards
                AdvantageItem(
                    title = "1. Single-Screen Infographic Poster",
                    description = "Unlike conventional finance tools that bury budgets across multiple nested menus, SalaryLane presents your complete monthly financial allocation on one executive board.",
                    icon = Icons.Rounded.ElectricBolt,
                    iconBg = MoneyBoardColors.BadgeAmber
                )

                AdvantageItem(
                    title = "2. 100% Offline & Private by Design",
                    description = "No mandatory account creation, zero cloud data harvesting, and no SMS scraping. Your financial records reside exclusively in your local on-device SQLite database.",
                    icon = Icons.Rounded.Lock,
                    iconBg = MoneyBoardColors.BadgeTeal
                )

                AdvantageItem(
                    title = "3. Zero Subscriptions, 100% Free",
                    description = "Enjoy unlimited monthly budget planning, transaction tracking, and high-fidelity report generation without recurring fees or locked paywalls.",
                    icon = Icons.Rounded.MoneyOff,
                    iconBg = MoneyBoardColors.BadgeGreen
                )

                AdvantageItem(
                    title = "4. Tailored for Indian Household Reality",
                    description = "Native, dedicated support for SIPs, LIC policies, Family Support, House Help, and amortized annual expenses (Yearly ÷ 12).",
                    icon = Icons.Rounded.CheckCircle,
                    iconBg = MoneyBoardColors.BadgeBlue
                )

                AdvantageItem(
                    title = "5. Professional Vector PDF & Image Export",
                    description = "Generate publication-quality A4 infographic reports and CSV summaries ready to share with your family or accountant in one tap.",
                    icon = Icons.Rounded.PictureAsPdf,
                    iconBg = MoneyBoardColors.BadgeRed
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun AdvantageItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconBg: Color
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.SalarySurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )
            }
        }
    }
}

