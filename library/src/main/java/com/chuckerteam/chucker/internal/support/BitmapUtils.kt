package com.chuckerteam.chucker.internal.support

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private val BITMAP_PAINT = Paint(Paint.FILTER_BITMAP_FLAG)

internal suspend fun Bitmap.calculateLuminance(): Double? {
    val color = Color.MAGENTA
    val alphaColouredBitmap = withContext(Dispatchers.Default) {
        return@withContext replaceAlphaWithColor(color)
    }
    return alphaColouredBitmap.getLuminance(color)
}

private fun Bitmap.replaceAlphaWithColor(@ColorInt color: Int): Bitmap {
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    result.eraseColor(color)
    Canvas(result).apply {
        drawBitmap(this@replaceAlphaWithColor, Matrix(), BITMAP_PAINT)
    }
    return result
}

private suspend fun Bitmap.getLuminance(@ColorInt alphaSubstitute: Int): Double? {
    return suspendCancellableCoroutine { continuation ->
        val luminanceTask = Palette.from(this)
            .clearFilters()
            .addFilter { rgb, _ -> rgb != alphaSubstitute }
            .generate { palette ->
                val dominantSwatch = palette?.dominantSwatch
                val luminance = dominantSwatch?.rgb?.let { ColorUtils.calculateLuminance(it) }
                continuation.resume(luminance)
            }
        continuation.invokeOnCancellation { luminanceTask.cancel(true) }
    }
}
