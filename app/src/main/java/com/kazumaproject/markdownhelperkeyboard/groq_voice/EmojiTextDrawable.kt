package com.kazumaproject.markdownhelperkeyboard.groq_voice

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Draws an emoji (rendered by the system emoji font) as a key icon. Color filters and
 * tints are ignored so the emoji keeps its own colors on themed keys.
 */
class EmojiTextDrawable(
    private val text: String,
    private val intrinsicSizePx: Int,
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    override fun draw(canvas: Canvas) {
        val area = bounds
        if (area.isEmpty) return
        paint.textSize = min(area.width(), area.height()) * 0.8f
        val baseline = area.exactCenterY() - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, area.exactCenterX(), baseline, paint)
    }

    override fun getIntrinsicWidth(): Int = intrinsicSizePx

    override fun getIntrinsicHeight(): Int = intrinsicSizePx

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
