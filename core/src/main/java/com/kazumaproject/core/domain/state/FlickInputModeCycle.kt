package com.kazumaproject.core.domain.state

/**
 * フリック系キーボード（TenKey / 五十音 / Sumire）の入力モード巡回。
 * 英字モードは巡回に含めず、日本語 ⇔ 数字 の2状態で切り替える。
 * 旧バージョンの保存状態などで英字が渡された場合は日本語へ戻す。
 */
fun nextFlickInputMode(current: InputMode): InputMode = when (current) {
    InputMode.ModeJapanese -> InputMode.ModeNumber
    InputMode.ModeNumber -> InputMode.ModeJapanese
    InputMode.ModeEnglish -> InputMode.ModeJapanese
}

/** フリック系キーボードで英字モードを表示しないための正規化。 */
fun InputMode.normalizedForFlick(): InputMode = when (this) {
    InputMode.ModeEnglish -> InputMode.ModeJapanese
    else -> this
}
