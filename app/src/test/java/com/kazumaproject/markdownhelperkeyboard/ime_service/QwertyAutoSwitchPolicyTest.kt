package com.kazumaproject.markdownhelperkeyboard.ime_service

import com.kazumaproject.markdownhelperkeyboard.ime_service.state.InputTypeForIME
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QwertyAutoSwitchPolicyTest {

    @Test
    fun passwordTypesSwitchToQwertyWhenEnabled() {
        listOf(
            InputTypeForIME.TextPassword,
            InputTypeForIME.TextWebPassword,
            InputTypeForIME.TextVisiblePassword,
        ).forEach {
            assertTrue("$it", QwertyAutoSwitchPolicy.shouldUseQwerty(it, enabled = true))
        }
    }

    @Test
    fun emailTypesSwitchToQwertyWhenEnabled() {
        listOf(
            InputTypeForIME.TextEmailAddress,
            InputTypeForIME.TextWebEmailAddress,
        ).forEach {
            assertTrue("$it", QwertyAutoSwitchPolicy.shouldUseQwerty(it, enabled = true))
        }
    }

    @Test
    fun nothingSwitchesWhenDisabled() {
        listOf(
            InputTypeForIME.TextPassword,
            InputTypeForIME.TextEmailAddress,
            InputTypeForIME.TextWebEmailAddress,
        ).forEach {
            assertFalse("$it", QwertyAutoSwitchPolicy.shouldUseQwerty(it, enabled = false))
        }
    }

    @Test
    fun numberAndPlainTextTypesNeverSwitch() {
        listOf(
            InputTypeForIME.NumberPassword,
            InputTypeForIME.Number,
            InputTypeForIME.Phone,
            InputTypeForIME.Text,
        ).forEach {
            assertFalse("$it", QwertyAutoSwitchPolicy.shouldUseQwerty(it, enabled = true))
        }
    }

    @Test
    fun autoSwitchTypesContainPasswordAndEmailOnly() {
        assertTrue(
            QwertyAutoSwitchPolicy.types.containsAll(
                setOf(
                    InputTypeForIME.TextPassword,
                    InputTypeForIME.TextWebPassword,
                    InputTypeForIME.TextVisiblePassword,
                    InputTypeForIME.TextEmailAddress,
                    InputTypeForIME.TextWebEmailAddress,
                )
            )
        )
        assertFalse(InputTypeForIME.NumberPassword in QwertyAutoSwitchPolicy.types)
    }
}
