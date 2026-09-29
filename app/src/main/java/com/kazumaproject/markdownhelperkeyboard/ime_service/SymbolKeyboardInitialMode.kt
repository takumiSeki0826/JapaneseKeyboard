package com.kazumaproject.markdownhelperkeyboard.ime_service

import com.kazumaproject.core.data.clicked_symbol.SymbolMode

/** Tab the symbol keyboard opens on: clipboard when explicitly requested, else the configured first tab. */
internal fun resolveSymbolKeyboardInitialMode(
    requestedMode: SymbolMode?,
    firstItem: SymbolMode?,
): SymbolMode = if (requestedMode == SymbolMode.CLIPBOARD) SymbolMode.CLIPBOARD else firstItem ?: SymbolMode.EMOJI
