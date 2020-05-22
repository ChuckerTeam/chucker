package com.chuckerteam.chucker.internal.support

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader.TileMode.REPEAT
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

internal class ChessboardDrawable(
    @ColorInt evenColor: Int,
    @ColorInt oddColor: Int,
    @Px squareSize: Int
) : Drawable() {
    private val chessboardPaint = Paint().apply {
        val patternBitmap = Bitmap.createBitmap(squareSize * 2, squareSize * 2, ARGB_8888)
        patternBitmap.eraseColor(evenColor)

        color = oddColor
        style = FILL
        val patternCanvas = Canvas(patternBitmap)
        val squareRect = Rect(squareSize, 0, 2 * squareSize, squareSize)
        patternCanvas.drawRect(squareRect, this)
        squareRect.offsetTo(0, squareSize)
        patternCanvas.drawRect(squareRect, this)

        reset()
        shader = BitmapShader(patternBitmap, REPEAT, REPEAT)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(chessboardPaint)
    }

    override fun setAlpha(alpha: Int) {
        chessboardPaint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return if (chessboardPaint.colorFilter == null) {
            PixelFormat.OPAQUE
        } else {
            PixelFormat.TRANSLUCENT
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        chessboardPaint.colorFilter = colorFilter
    }

    companion object {
        fun createPattern(
            context: Context,
            @ColorRes evenColorId: Int,
            @ColorRes oddColorId: Int,
            @DimenRes sizeId: Int
        ): ChessboardDrawable {
            val evenColor = ContextCompat.getColor(context, evenColorId)
            val oddColor = ContextCompat.getColor(context, oddColorId)
            val size = context.resources.getDimensionPixelSize(sizeId)
            return ChessboardDrawable(evenColor, oddColor, size)
        }
    }
}
