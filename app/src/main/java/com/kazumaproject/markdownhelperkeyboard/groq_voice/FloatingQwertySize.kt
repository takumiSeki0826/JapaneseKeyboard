package com.kazumaproject.markdownhelperkeyboard.groq_voice

data class KeyboardSize(val widthPercent: Int, val heightDp: Int)

/**
 * In the Groq build the floating keyboard keeps one size: switching to QWERTY reuses the
 * TenKey (Japanese) width and height instead of QWERTY's own, usually full-width, size.
 */
object FloatingQwertySize {
    fun resolve(
        matchTenkeyInFloating: Boolean,
        floating: Boolean,
        tenkey: KeyboardSize,
        qwerty: KeyboardSize,
    ): KeyboardSize = if (matchTenkeyInFloating && floating) tenkey else qwerty
}
