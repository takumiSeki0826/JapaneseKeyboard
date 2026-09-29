package com.kazumaproject.markdownhelperkeyboard.ime_service

import com.kazumaproject.core.data.clicked_symbol.SymbolMode
import org.junit.Assert.assertEquals
import org.junit.Test

class SymbolKeyboardInitialModeTest {

    @Test
    fun clipboardRequestOpensClipboard() {
        assertEquals(
            SymbolMode.CLIPBOARD,
            resolveSymbolKeyboardInitialMode(requestedMode = SymbolMode.CLIPBOARD, firstItem = SymbolMode.EMOJI),
        )
    }

    @Test
    fun otherRequestsOpenConfiguredFirstTab() {
        assertEquals(
            SymbolMode.SYMBOL,
            resolveSymbolKeyboardInitialMode(requestedMode = SymbolMode.EMOJI, firstItem = SymbolMode.SYMBOL),
        )
    }

    @Test
    fun missingFirstTabFallsBackToEmoji() {
        assertEquals(
            SymbolMode.EMOJI,
            resolveSymbolKeyboardInitialMode(requestedMode = null, firstItem = null),
        )
    }
}
