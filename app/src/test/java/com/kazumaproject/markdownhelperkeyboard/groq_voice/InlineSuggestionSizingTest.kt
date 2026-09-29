package com.kazumaproject.markdownhelperkeyboard.groq_voice

import com.kazumaproject.markdownhelperkeyboard.ime_service.autofill.InlineSuggestionSizing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InlineSuggestionSizingTest {

    @Test
    fun compactStripKeepsChipInsideStrip() {
        val sizing = InlineSuggestionSizing.forBuild(useGroqVoice = true)

        assertTrue(sizing.chipHeightDp < 36)
        assertEquals(null, sizing.containerHeightDp)
    }

    @Test
    fun upstreamSizingIsUnchanged() {
        val sizing = InlineSuggestionSizing.forBuild(useGroqVoice = false)

        assertEquals(48, sizing.chipHeightDp)
        assertEquals(58, sizing.containerHeightDp)
    }
}
