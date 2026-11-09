package com.kazumaproject.core.ui.input_mode_witch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.kazumaproject.core.R
import com.kazumaproject.core.domain.state.InputMode
import com.kazumaproject.core.domain.state.TwoStateNumberReturnTarget
import com.kazumaproject.core.ui.font.KeyboardFontAware
import com.kazumaproject.core.ui.font.KeyboardFontApplicator
import com.kazumaproject.core.ui.font.KeyboardFontGlyphDrawable
import com.kazumaproject.core.ui.font.KeyboardFontSnapshot

class InputModeSwitch(context: Context, attrs: AttributeSet) :
    AppCompatImageButton(context, attrs), KeyboardFontAware {

    private var currentInputMode: InputMode = InputMode.ModeJapanese
    private var isGojuonMode = false
    private var useThreeStateKeyboard = true
    private var numberReturnTarget = TwoStateNumberReturnTarget.Japanese
    private var fontSnapshot = KeyboardFontSnapshot()
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    fun setInputMode(inputMode: InputMode, isGojuon: Boolean) {
        setInputMode(inputMode, isGojuon, useThreeStateKeyboard = true)
    }

    fun setInputMode(
        inputMode: InputMode,
        isGojuon: Boolean,
        useThreeStateKeyboard: Boolean,
        twoStateNumberReturnTarget: TwoStateNumberReturnTarget = TwoStateNumberReturnTarget.Japanese
    ) {
        currentInputMode = inputMode
        this.isGojuonMode = isGojuon
        this.useThreeStateKeyboard = useThreeStateKeyboard
        this.numberReturnTarget = twoStateNumberReturnTarget
        renderModeIcon()
    }

    override fun setKeyboardFont(snapshot: KeyboardFontSnapshot) {
        KeyboardFontApplicator.track(this)
        if (fontSnapshot == snapshot) return
        fontSnapshot = snapshot
        labelPaint.typeface = snapshot.typeface?.let { Typeface.create(it, Typeface.NORMAL) }
        renderModeIcon()
    }

    private fun renderModeIcon() {
        // 英字モードを廃止したため、切り替えキーは常に「あ・1」のラベルを自前で描画する。
        setImageDrawable(null)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val iconWidth = width - paddingLeft - paddingRight
        val iconHeight = height - paddingTop - paddingBottom
        if (iconWidth <= 0 || iconHeight <= 0) return
        val fallbackColor = ContextCompat.getColor(context, R.color.keyboard_icon_color)
        val tint = imageTintList?.getColorForState(drawableState, fallbackColor)
        val selectedColor = tint ?: ContextCompat.getColor(context, R.color.keyboard_icon_color)
        val idleColor = tint ?: 0xff839096.toInt()
        // フリック系キーボードの入力モードは「あ」「1」の2状態。旧状態の英字は「あ」として扱う。
        val selectedIndex = if (currentInputMode == InputMode.ModeNumber) 1 else 0
        val labels = listOf("あ", "1")
        val positions = floatArrayOf(.38f, .62f)
        val glyphHeight = iconHeight * 0.44f
        val glyphWidth = iconWidth * 0.22f
        val typeface = fontSnapshot.typeface ?: Typeface.DEFAULT
        // 「あ」と「1」は同じ文字サイズで描く（個別に区画へ拡大すると、縦長の「1」だけ大きくなる）。
        val textSize = fitTextSize(labels.first(), typeface, glyphWidth, glyphHeight)
        val bounds = Rect()
        labels.forEachIndexed { index, label ->
            val baseColor = if (index == selectedIndex) selectedColor else idleColor
            labelPaint.color = Color.argb(
                Color.alpha(baseColor) * imageAlpha / 255,
                Color.red(baseColor),
                Color.green(baseColor),
                Color.blue(baseColor),
            )
            labelPaint.typeface = Typeface.create(
                typeface,
                if (index == selectedIndex) Typeface.BOLD else Typeface.NORMAL
            )
            labelPaint.textAlign = Paint.Align.LEFT
            labelPaint.textSize = textSize
            labelPaint.getTextBounds(label, 0, label.length, bounds)
            val centerX = paddingLeft + iconWidth * positions[index]
            val centerY = paddingTop + iconHeight * 0.5f
            canvas.drawText(
                label,
                centerX - (bounds.left + bounds.right) / 2f,
                centerY - (bounds.top + bounds.bottom) / 2f,
                labelPaint
            )
        }
    }

    private fun fitTextSize(text: String, typeface: Typeface, maxWidth: Float, maxHeight: Float): Float {
        labelPaint.typeface = typeface
        labelPaint.textSize = 100f
        val bounds = Rect()
        labelPaint.getTextBounds(text, 0, text.length, bounds)
        val scale = minOf(
            maxWidth / bounds.width().coerceAtLeast(1),
            maxHeight / bounds.height().coerceAtLeast(1)
        )
        return (100f * scale).coerceAtLeast(1f)
    }
}

fun resolveInputModeSwitchIconResId(
    inputMode: InputMode,
    isGojuon: Boolean,
    useThreeStateKeyboard: Boolean,
    twoStateNumberReturnTarget: TwoStateNumberReturnTarget = TwoStateNumberReturnTarget.Japanese
): Int {
    return if (useThreeStateKeyboard) {
        when (inputMode) {
            InputMode.ModeJapanese -> if (isGojuon) {
                R.drawable.input_mode_japanese_select_gojuon
            } else {
                R.drawable.input_mode_japanese_select
            }

            InputMode.ModeEnglish -> if (isGojuon) {
                R.drawable.input_mode_english_select_gojuon
            } else {
                R.drawable.input_mode_english_select
            }

            InputMode.ModeNumber -> if (isGojuon) {
                R.drawable.input_mode_number_select_gojuon
            } else {
                R.drawable.input_mode_number_select
            }
        }
    } else {
        when (inputMode) {
            InputMode.ModeJapanese -> R.drawable.language_japanese_kana_left_bold_24px
            InputMode.ModeEnglish -> R.drawable.language_japanese_kana_right_bold_24px
            InputMode.ModeNumber -> when (twoStateNumberReturnTarget) {
                TwoStateNumberReturnTarget.Japanese -> R.drawable.input_mode_japanese_select_custom
                TwoStateNumberReturnTarget.English -> R.drawable.input_mode_english_custom
            }
        }
    }
}
