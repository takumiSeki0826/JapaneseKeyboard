package com.kazumaproject.markdownhelperkeyboard.ime_service.autofill

import com.kazumaproject.markdownhelperkeyboard.BuildConfig

/**
 * Sizes for inline autofill chips (e.g. 1Password). The Groq build's candidate strip is
 * only 36dp tall, so chips shrink and the container follows the strip height (null).
 */
internal data class InlineSuggestionSizing(
    val chipHeightDp: Int,
    val containerHeightDp: Int?,
) {
    companion object {
        val current: InlineSuggestionSizing get() = forBuild(BuildConfig.USE_GROQ_VOICE)

        fun forBuild(useGroqVoice: Boolean): InlineSuggestionSizing =
            if (useGroqVoice) {
                InlineSuggestionSizing(chipHeightDp = 28, containerHeightDp = null)
            } else {
                InlineSuggestionSizing(chipHeightDp = 48, containerHeightDp = 58)
            }
    }
}
