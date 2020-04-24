package com.chuckerteam.chucker.internal.support

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val BITMAP_PAINT = Paint(Paint.FILTER_BITMAP_FLAG)

internal suspend fun Bitmap.calculateLuminance(): Double? {
    val color = Color.MAGENTA
    return withContext(Dispatchers.Default) {
        val alpha = replaceAlphaWithColor(color)
        return@withContext alpha.getLuminance(color)
    }
}

private fun Bitmap.replaceAlphaWithColor(@ColorInt color: Int): Bitmap {
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    result.eraseColor(color)
    Canvas(result).apply {
        drawBitmap(this@replaceAlphaWithColor, Matrix(), BITMAP_PAINT)
    }
    return result
}

private fun Bitmap.getLuminance(@ColorInt alphaSubstitute: Int): Double? {
    val imagePalette = Palette.from(this)
        .clearFilters()
        .addFilter { rgb, _ -> (rgb != alphaSubstitute) }
        .generate()
    val dominantSwatch = imagePalette.dominantSwatch
    return dominantSwatch?.rgb?.let(ColorUtils::calculateLuminance)
}
