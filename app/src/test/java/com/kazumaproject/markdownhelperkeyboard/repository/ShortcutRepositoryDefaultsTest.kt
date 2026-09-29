package com.kazumaproject.markdownhelperkeyboard.repository

import com.kazumaproject.markdownhelperkeyboard.short_cut.ShortcutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShortcutRepositoryDefaultsTest {

    @Test
    fun groqDefaultsPlaceVoiceInputRightAfterClipboard() {
        val defaults = ShortcutRepository.defaultShortcutsFor(useGroqVoice = true)

        assertEquals(
            listOf(
                ShortcutType.HIDE_KEYBOARD,
                ShortcutType.CLIP_BOARD,
                ShortcutType.VOICE_INPUT,
                ShortcutType.TEMPLATE,
                ShortcutType.KEYBOARD_LAYOUT_EDIT,
            ),
            defaults,
        )
    }

    @Test
    fun upstreamDefaultsAreUnchanged() {
        val defaults = ShortcutRepository.defaultShortcutsFor(useGroqVoice = false)

        assertFalse(ShortcutType.VOICE_INPUT in defaults)
        assertEquals(9, defaults.size)
    }
}
