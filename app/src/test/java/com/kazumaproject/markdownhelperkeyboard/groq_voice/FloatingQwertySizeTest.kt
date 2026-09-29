package com.kazumaproject.markdownhelperkeyboard.groq_voice

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingQwertySizeTest {

    private val tenkey = KeyboardSize(widthPercent = 32, heightDp = 155)
    private val qwerty = KeyboardSize(widthPercent = 100, heightDp = 280)

    @Test
    fun floatingGroqQwertyUsesTenkeySize() {
        val size = FloatingQwertySize.resolve(
            matchTenkeyInFloating = true,
            floating = true,
            tenkey = tenkey,
            qwerty = qwerty,
        )

        assertEquals(tenkey, size)
    }

    @Test
    fun dockedKeyboardKeepsQwertySize() {
        val size = FloatingQwertySize.resolve(
            matchTenkeyInFloating = true,
            floating = false,
            tenkey = tenkey,
            qwerty = qwerty,
        )

        assertEquals(qwerty, size)
    }

    @Test
    fun upstreamBuildKeepsQwertySize() {
        val size = FloatingQwertySize.resolve(
            matchTenkeyInFloating = false,
            floating = true,
            tenkey = tenkey,
            qwerty = qwerty,
        )

        assertEquals(qwerty, size)
    }
}
