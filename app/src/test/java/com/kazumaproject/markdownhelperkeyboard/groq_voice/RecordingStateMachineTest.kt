package com.kazumaproject.markdownhelperkeyboard.groq_voice

import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingStateMachineTest {

    @Test
    fun `tapping mic while idle starts recording`() {
        val next = RecordingStateMachine.reduce(RecordingState.Idle, RecordingEvent.MicTapped)

        assertEquals(RecordingState.Recording, next)
    }

    @Test
    fun `tapping mic while recording moves to transcribing`() {
        val next = RecordingStateMachine.reduce(RecordingState.Recording, RecordingEvent.MicTapped)

        assertEquals(RecordingState.Transcribing, next)
    }

    @Test
    fun `tapping mic while transcribing is ignored`() {
        val next = RecordingStateMachine.reduce(RecordingState.Transcribing, RecordingEvent.MicTapped)

        assertEquals(RecordingState.Transcribing, next)
    }

    @Test
    fun `transcription success while transcribing returns to idle`() {
        val next = RecordingStateMachine.reduce(
            RecordingState.Transcribing,
            RecordingEvent.TranscriptionSucceeded("こんにちは"),
        )

        assertEquals(RecordingState.Idle, next)
    }

    @Test
    fun `transcription failure while transcribing moves to error`() {
        val next = RecordingStateMachine.reduce(
            RecordingState.Transcribing,
            RecordingEvent.TranscriptionFailed("network error"),
        )

        assertEquals(RecordingState.Error("network error"), next)
    }

    @Test
    fun `tapping mic while in error state starts a fresh recording`() {
        val next = RecordingStateMachine.reduce(
            RecordingState.Error("network error"),
            RecordingEvent.MicTapped,
        )

        assertEquals(RecordingState.Recording, next)
    }

    @Test
    fun `error state auto-recovers to idle when acknowledged`() {
        val next = RecordingStateMachine.reduce(
            RecordingState.Error("network error"),
            RecordingEvent.ErrorAcknowledged,
        )

        assertEquals(RecordingState.Idle, next)
    }
}
