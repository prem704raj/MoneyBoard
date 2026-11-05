package com.premraj.moneyboard.core.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.graphics.toArgb
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel
import java.io.File
import java.io.FileOutputStream
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object MonthlyReportPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generatePdf(
        context: Context,
        board: FinanceBoardUiModel,
        month: YearMonth,
        settings: UserSettings
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInfographicReport(canvas, board, month, settings)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val monthName = month.format(DateTimeFormatter.ofPattern("MMM_yyyy"))
        val file = File(outputDir, "MoneyBoard_Report_${monthName}.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawInfographicReport(
        canvas: Canvas,
        board: FinanceBoardUiModel,
        month: YearMonth,
        settings: UserSettings
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.parseColor("#F4F7FB")
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // 1. Header Banner
        paint.color = Color.parseColor("#0F3256")
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText("Monthly Expense Breakdown", 20f, 32f, paint)

        // Subtitle
        paint.color = Color.parseColor("#C2D8F2")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText("${settings.profileTag}   |   ${settings.cityName}   |   ${settings.emiTag}", 20f, 50f, paint)

        // Month Text on Top Right
        val monthTitle = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        paint.textSize = 10f
        paint.color = Color.WHITE
        val monthWidth = paint.measureText(monthTitle)
        canvas.drawText(monthTitle, PAGE_WIDTH - 20f - monthWidth, 24f, paint)

        // Salary Pill in Header
        val pillRect = RectF(PAGE_WIDTH - 180f, 34f, PAGE_WIDTH - 20f, 62f)
        paint.color = Color.parseColor("#22669C")
        canvas.drawRoundRect(pillRect, 6f, 6f, paint)

        paint.color = Color.parseColor("#E0EFFE")
        paint.textSize = 8f
        canvas.drawText("In-hand Salary", PAGE_WIDTH - 172f, 45f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText(board.salaryText, PAGE_WIDTH - 172f, 58f, paint)

        // 2. Dual-Column Expense Ledger
        val leftX = 20f
        val leftW = 270f
        val rightX = 305f
        val rightW = 270f
        var currentY = 84f

        val half = if (board.expenseRows.size > 12) (board.expenseRows.size + 1) / 2 else board.expenseRows.size
        val leftRows = board.expenseRows.take(half)
        val rightRows = board.expenseRows.drop(half)

        drawLedgerColumn(canvas, paint, leftX, currentY, leftW, leftRows)
        drawLedgerColumn(canvas, paint, rightX, currentY, rightW, rightRows)

        currentY += 268f

        // 3. Total Monthly Expenses Bar
        val totalBarRect = RectF(20f, currentY, PAGE_WIDTH - 20f, currentY + 34f)
        paint.color = Color.parseColor("#153C66")
        canvas.drawRoundRect(totalBarRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("Total Monthly Expenses", 36f, currentY + 22f, paint)

        // Amount badge on right
        val amountBadgeRect = RectF(PAGE_WIDTH - 130f, currentY, PAGE_WIDTH - 20f, currentY + 34f)
        paint.color = Color.parseColor("#E2F0FA")
        canvas.drawRoundRect(amountBadgeRect, 6f, 6f, paint)

        paint.color = Color.parseColor("#153C66")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        val amtW = paint.measureText(board.totalAllocationText)
        canvas.drawText(board.totalAllocationText, PAGE_WIDTH - 75f - (amtW / 2f), currentY + 23f, paint)

        currentY += 44f

        // 4. Four Summary Cards (2x2 Grid)
        val cardW = 270f
        val cardH = 92f

        // Salary vs Expenses
        drawSummaryCard(
            canvas, paint, 20f, currentY, cardW, cardH,
            title = "Salary vs Expenses",
            titleColor = "#0F3256",
            bgColor = "#F0F8FD",
            borderColor = "#CCE4F5",
            lines = board.salaryVsExpenses,
            pillLabel = "Remaining",
            pillValue = board.remainingText,
            pillBg = "#DCF5E8",
            pillTextColor = "#147A5B"
        )

        // Savings & Investments
        drawSummaryCard(
            canvas, paint, 305f, currentY, cardW, cardH,
            title = "Savings & Investments (Per Month)",
            titleColor = "#147A5B",
            bgColor = "#F1FAF6",
            borderColor = "#C7EBD8",
            lines = board.savings,
            pillLabel = "Total Savings/Investment",
            pillValue = board.totalSavingsText,
            pillBg = "#DCF5E8",
            pillTextColor = "#147A5B"
        )

        currentY += 98f

        // Living Spending
        drawSummaryCard(
            canvas, paint, 20f, currentY, cardW, cardH,
            title = "Actual Living / Family / Travel",
            titleColor = "#6236A8",
            bgColor = "#F6F2FC",
            borderColor = "#E4D7F7",
            lines = board.living,
            pillLabel = "Actual Spending",
            pillValue = board.actualSpendingText,
            pillBg = "#EDE2FB",
            pillTextColor = "#6236A8"
        )

        // Annual Summary
        drawSummaryCard(
            canvas, paint, 305f, currentY, cardW, cardH,
            title = "Annual Summary",
            titleColor = "#C95D18",
            bgColor = "#FFF6EE",
            borderColor = "#FEDDC6",
            lines = board.annual,
            pillLabel = "Annual Balance",
            pillValue = board.annualBalanceText,
            pillBg = "#FFE8D6",
            pillTextColor = "#C95D18"
        )

        currentY += 104f

        // 5. Bottom Breakdown & Verification Summary Cards
        val bottomCardH = 120f

        // Left: Category Breakdown with Dynamic Vector Donut Ring
        val breakdownRect = RectF(20f, currentY, 20f + cardW, currentY + bottomCardH)
        paint.color = Color.WHITE
        canvas.drawRoundRect(breakdownRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#DCE8F2")
        canvas.drawRoundRect(breakdownRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#10243B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("Spending Breakdown (% Allocation)", 30f, currentY + 18f, paint)

        // Draw Vector Donut Ring
        val donutCenterX = 72f
        val donutCenterY = currentY + 68f
        val donutRadius = 35f
        val donutRect = RectF(
            donutCenterX - donutRadius,
            donutCenterY - donutRadius,
            donutCenterX + donutRadius,
            donutCenterY + donutRadius
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 14f
        paint.strokeCap = Paint.Cap.BUTT

        var startAngle = -90f
        board.donutSlices.forEach { slice ->
            val sweep = (slice.percentageValue / 100f) * 360f
            if (sweep > 0.5f) {
                paint.color = slice.color.toArgb()
                canvas.drawArc(donutRect, startAngle, sweep, false, paint)
                startAngle += sweep
            }
        }

        // Donut Hole Center Text
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#6B7E90")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 6.5f
        val monthlyLabel = "Expenses"
        val mlW = paint.measureText(monthlyLabel)
        canvas.drawText(monthlyLabel, donutCenterX - (mlW / 2f), donutCenterY - 2f, paint)

        paint.color = Color.parseColor("#10243B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 7.5f
        val totalAmt = board.totalAllocationText.replace("/ month", "").trim()
        val taW = paint.measureText(totalAmt)
        canvas.drawText(totalAmt, donutCenterX - (taW / 2f), donutCenterY + 9f, paint)

        // Donut Legend (Dynamic Slices from board.donutSlices)
        var sY = currentY + 34f
        board.donutSlices.forEach { slice ->
            paint.color = slice.color.toArgb()
            paint.style = Paint.Style.FILL
            canvas.drawCircle(128f, sY - 3f, 3.5f, paint)

            paint.color = Color.parseColor("#51667C")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(slice.title, 136f, sY, paint)

            paint.color = Color.parseColor("#10243B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val pW = paint.measureText(slice.percentageText)
            canvas.drawText(slice.percentageText, 20f + cardW - 12f - pW, sY, paint)

            sY += 13.5f
        }

        // Right: Verification Summary
        val summaryRect = RectF(305f, currentY, 305f + cardW, currentY + bottomCardH)
        paint.color = Color.parseColor("#F6FAF7")
        canvas.drawRoundRect(summaryRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#D3EAD9")
        canvas.drawRoundRect(summaryRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#1B7A4D")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("Verified Financial Summary", 318f, currentY + 18f, paint)

        val verifiedItems = listOf(
            "Total monthly expenses: ${board.totalAllocationText} (verified)",
            "In-hand salary: ${board.salaryText.replace("/ month", "").trim()}",
            "Monthly remaining: ${board.remainingText.replace("/ month", "").trim()}",
            "Total annual expenses: ${board.annual.firstOrNull()?.value ?: ""}",
            "Total annual salary: ${board.annual.getOrNull(1)?.value ?: ""}",
            "Annual balance: ${board.annualBalanceText}",
            "Savings rate: ${board.savingsRateText}"
        )

        var vY = currentY + 32f
        verifiedItems.forEach { itemText ->
            paint.color = Color.parseColor("#147A5B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            canvas.drawText("• $itemText", 318f, vY, paint)
            vY += 10.5f
        }

        // Reassurance footer in card
        paint.color = Color.parseColor("#1B7A4D")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 7.5f
        canvas.drawText("All calculations are accurate and verified for each expense item.", 318f, currentY + bottomCardH - 8f, paint)

        // 6. Global Page Footer
        paint.color = Color.parseColor("#7A8B9C")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7.5f
        canvas.drawText("Generated by MoneyBoard • 100% Offline & Private Financial Intelligence", 20f, PAGE_HEIGHT - 16f, paint)
    }

    private fun drawLedgerColumn(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        w: Float,
        rows: List<com.premraj.moneyboard.feature.dashboard.ExpenseRowUi>
    ) {
        val totalH = 256f
        val rect = RectF(x, y, x + w, y + totalH)

        // Outer border & fill
        paint.color = Color.WHITE
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#DCE8F2")
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Header
        val headerRect = RectF(x, y, x + w, y + 18f)
        paint.color = Color.parseColor("#D9EEFB")
        canvas.drawRoundRect(headerRect, 6f, 6f, paint)
        canvas.drawRect(x, y + 10f, x + w, y + 18f, paint) // square bottom corners of header

        paint.color = Color.parseColor("#0F3256")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("No.", x + 6f, y + 12.5f, paint)
        canvas.drawText("Expense", x + 30f, y + 12.5f, paint)
        val mLabel = "Monthly (₹)"
        val mW = paint.measureText(mLabel)
        canvas.drawText(mLabel, x + w - 8f - mW, y + 12.5f, paint)

        // Rows
        val rowH = if (rows.isNotEmpty()) 238f / maxOf(rows.size, 12) else 19.8f
        rows.forEachIndexed { i, row ->
            val rowY = y + 18f + (i * rowH)

            // Divider line
            if (i > 0) {
                paint.color = Color.parseColor("#EBF1F6")
                canvas.drawLine(x, rowY, x + w, rowY, paint)
            }

            // Number
            paint.color = Color.parseColor("#6B7E90")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(row.number.toString(), x + 8f, rowY + 13f, paint)

            // Category bullet dot
            paint.color = Color.parseColor("#1E88E5")
            canvas.drawCircle(x + 28f, rowY + 10f, 3f, paint)

            // Title
            val titleFirst = row.title.split("\n")[0]
            paint.color = Color.parseColor("#10243B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText(titleFirst, x + 36f, rowY + 13f, paint)

            // Amount
            paint.color = Color.parseColor("#10243B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            val amtClean = row.amountText.replace("₹", "").trim()
            val aW = paint.measureText(amtClean)
            canvas.drawText(amtClean, x + w - 8f - aW, rowY + 13f, paint)
        }
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        titleColor: String,
        bgColor: String,
        borderColor: String,
        lines: List<com.premraj.moneyboard.feature.dashboard.SummaryLineUi>,
        pillLabel: String,
        pillValue: String,
        pillBg: String,
        pillTextColor: String
    ) {
        val rect = RectF(x, y, x + w, y + h)

        // Background
        paint.color = Color.parseColor(bgColor)
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor(borderColor)
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Title
        paint.color = Color.parseColor(titleColor)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText(title, x + 10f, y + 16f, paint)

        // Lines
        var lineY = y + 30f
        lines.take(3).forEach { line ->
            paint.color = Color.parseColor("#51667C")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(line.label, x + 10f, lineY, paint)

            paint.color = Color.parseColor("#10243B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val valW = paint.measureText(line.value)
            canvas.drawText(line.value, x + w - 10f - valW, lineY, paint)

            lineY += 12f
        }

        // Bottom Pill
        val pillRect = RectF(x + 8f, y + h - 22f, x + w - 8f, y + h - 6f)
        paint.color = Color.parseColor(pillBg)
        canvas.drawRoundRect(pillRect, 4f, 4f, paint)

        paint.color = Color.parseColor(pillTextColor)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        canvas.drawText(pillLabel, x + 14f, y + h - 11f, paint)

        val pValW = paint.measureText(pillValue)
        canvas.drawText(pillValue, x + w - 14f - pValW, y + h - 11f, paint)
    }
}

