package com.kazumaproject.core.domain.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FlickInputModeCycleTest {

    @Test
    fun japaneseGoesToNumber() {
        assertEquals(InputMode.ModeNumber, nextFlickInputMode(InputMode.ModeJapanese))
    }

    @Test
    fun numberGoesBackToJapanese() {
        assertEquals(InputMode.ModeJapanese, nextFlickInputMode(InputMode.ModeNumber))
    }

    @Test
    fun legacyEnglishStateRecoversToJapanese() {
        assertEquals(InputMode.ModeJapanese, nextFlickInputMode(InputMode.ModeEnglish))
    }

    @Test
    fun cycleNeverVisitsEnglish() {
        var mode: InputMode = InputMode.ModeJapanese
        repeat(10) {
            mode = nextFlickInputMode(mode)
            assertNotEquals(InputMode.ModeEnglish, mode)
        }
    }

    @Test
    fun englishIsNormalizedToJapanese() {
        assertEquals(InputMode.ModeJapanese, InputMode.ModeEnglish.normalizedForFlick())
        assertEquals(InputMode.ModeJapanese, InputMode.ModeJapanese.normalizedForFlick())
        assertEquals(InputMode.ModeNumber, InputMode.ModeNumber.normalizedForFlick())
    }
}
