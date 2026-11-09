package com.kazumaproject.markdownhelperkeyboard.ime_service

import com.kazumaproject.markdownhelperkeyboard.ime_service.state.InputTypeForIME

/**
 * 入力欄の種類に応じて、フリックの英字入力ではなく QWERTY キーボードを自動表示するかの判定。
 * 対象はパスワード欄（数字のみの PIN は除く）とメールアドレス欄。
 */
object QwertyAutoSwitchPolicy {
    private val passwordTypes = setOf(
        InputTypeForIME.TextWebPassword,
        InputTypeForIME.TextPassword,
        InputTypeForIME.TextVisiblePassword,
    )

    private val emailTypes = setOf(
        InputTypeForIME.TextEmailAddress,
        InputTypeForIME.TextWebEmailAddress,
    )

    val types: Set<InputTypeForIME> = passwordTypes + emailTypes

    fun shouldUseQwerty(inputType: InputTypeForIME, enabled: Boolean): Boolean =
        enabled && inputType in types
}
