package com.kazumaproject.markdownhelperkeyboard.short_cut

import com.kazumaproject.markdownhelperkeyboard.repository.ShortcutRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class ShortcutTypeTest {
    @Test
    fun idsAreUniqueSoSavedShortcutsResolveToOneType() {
        assertEquals(ShortcutType.entries.size, ShortcutType.entries.map { it.id }.toSet().size)
    }

    @Test
    fun globeIsStoredAndRestoredById() {
        assertEquals("globe", ShortcutType.GLOBE.id)
        assertSame(ShortcutType.GLOBE, ShortcutType.fromId("globe"))
    }

    @Test
    fun existingSavedIdsStillResolve() {
        listOf("settings", "emoji", "template", "keyboard_picker", "clip_board", "voice_input")
            .forEach { assertEquals(it, ShortcutType.fromId(it)?.id) }
    }

    @Test
    fun globeIsNotAddedToTheDefaultToolbars() {
        listOf(true, false).forEach { groq ->
            assertFalse(ShortcutRepository.defaultShortcutsFor(groq).contains(ShortcutType.GLOBE))
        }
    }
}
