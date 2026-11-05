package com.premraj.moneyboard.core.export

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Unified Export Manager for high-fidelity 1224px image and PDF reports.
 *
 * Captures offscreen Jetpack Compose composables to bitmap at exact 1224px width,
 * and saves PNG and PDF representations to Scoped Storage (Pictures/MoneyBoard and Documents/MoneyBoard)
 * without requiring runtime storage permissions on Android 10+ (API 29+).
 */
object ReportExportManager {

    /**
     * Renders an offscreen Composable to an unclipped, fully-measured [Bitmap] of exact [widthPx] width.
     * Attaches an invisible [ComposeView] to the activity root decorView, awaits two Choreographer
     * frames to guarantee full composition, measures, layouts, and draws to ARGB_8888 bitmap.
     */
    suspend fun captureComposableBitmap(
        activity: ComponentActivity,
        widthPx: Int = 1224,
        content: @Composable () -> Unit
    ): Bitmap = withContext(Dispatchers.Main) {
        val composeView = ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        }
        composeView.setViewTreeLifecycleOwner(activity)
        composeView.setViewTreeSavedStateRegistryOwner(activity)
        composeView.setViewTreeViewModelStoreOwner(activity)

        composeView.setContent(content)

        val root = activity.findViewById<ViewGroup>(android.R.id.content)
            ?: (activity.window.decorView as ViewGroup)
        composeView.visibility = View.INVISIBLE
        root.addView(composeView, ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT))

        try {
            // Await frames to ensure Compose composition, measure & layout passes commit
            awaitFrame()
            awaitFrame()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            composeView.measure(widthSpec, heightSpec)
            val measuredHeight = composeView.measuredHeight.coerceAtLeast(1)
            composeView.layout(0, 0, widthPx, measuredHeight)

            val bitmap = Bitmap.createBitmap(widthPx, measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            composeView.draw(canvas)
            bitmap
        } finally {
            root.removeView(composeView)
        }
    }

    /**
     * Saves the provided [bitmap] as a PNG image to `Pictures/MoneyBoard` via MediaStore.
     * Manages `IS_PENDING` on Android 10+ (API 29+) to ensure atomic write without runtime permissions.
     */
    fun savePngToMediaStore(
        activity: ComponentActivity,
        bitmap: Bitmap,
        filename: String = "MoneyBoard_Report_${System.currentTimeMillis()}.png"
    ): Uri? {
        val resolver = activity.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MoneyBoard")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            return null
        }
    }

    /**
     * Draws the provided [bitmap] 1:1 onto a [PdfDocument] page matching the bitmap dimensions,
     * and saves the resulting PDF to `Documents/MoneyBoard` via MediaStore.
     */
    fun savePdfToMediaStore(
        activity: ComponentActivity,
        bitmap: Bitmap,
        filename: String = "MoneyBoard_Report_${System.currentTimeMillis()}.pdf"
    ): Uri? {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        page.canvas.drawBitmap(bitmap, 0f, 0f, paint)
        pdfDoc.finishPage(page)

        val resolver = activity.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/MoneyBoard")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Files.getContentUri("external")

        val uri = resolver.insert(collection, values) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            pdfDoc.close()
            resolver.delete(uri, null, null)
            return null
        }
    }
}
