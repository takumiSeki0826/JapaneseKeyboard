package com.kazumaproject.markdownhelperkeyboard.groq_voice

import java.io.File
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceRecordingControllerTest {

    private class FakeRecorder : VoiceRecorder {
        var started = false
        var stopped = false
        var cancelled = false
        var startError: Exception? = null
        var stopResult: File? = File("recording.m4a")

        override fun start(): File {
            startError?.let { throw it }
            started = true
            return File("recording.m4a")
        }

        override fun stop(): File? {
            stopped = true
            return stopResult
        }

        override fun cancel() {
            cancelled = true
        }
    }

    private class FakeTranscriber : TranscriptionClient {
        val result = CompletableDeferred<Result<String>>()
        var calls = 0

        override suspend fun transcribe(audioFile: File): Result<String> {
            calls++
            return result.await()
        }
    }

    private class Harness(
        scope: TestScope,
        apiKeyConfigured: Boolean = true,
        micPermission: Boolean = true,
    ) {
        val recorder = FakeRecorder()
        val transcriber = FakeTranscriber()
        val states = mutableListOf<RecordingState>()
        val committed = mutableListOf<String>()
        val controller = VoiceRecordingController(
            recorder = recorder,
            transcriber = transcriber,
            scope = scope,
            isApiKeyConfigured = { apiKeyConfigured },
            hasMicPermission = { micPermission },
            onStateChanged = { states += it },
            onTranscribed = { committed += it },
        )
    }

    @Test
    fun `toggle while idle starts recording`() = runTest {
        val h = Harness(this)

        h.controller.toggle()

        assertTrue(h.recorder.started)
        assertEquals(RecordingState.Recording, h.controller.state)
    }

    @Test
    fun `second toggle stops recording and commits transcription`() = runTest {
        val h = Harness(this)
        h.controller.toggle()

        h.controller.toggle()
        runCurrent()
        assertTrue(h.recorder.stopped)
        assertEquals(RecordingState.Transcribing, h.controller.state)

        h.transcriber.result.complete(Result.success("こんにちは"))
        advanceUntilIdle()

        assertEquals(listOf("こんにちは"), h.committed)
        assertEquals(RecordingState.Idle, h.controller.state)
    }

    @Test
    fun `toggle while transcribing is ignored`() = runTest {
        val h = Harness(this)
        h.controller.toggle()
        h.controller.toggle()
        runCurrent()

        h.controller.toggle()
        runCurrent()

        assertEquals(1, h.transcriber.calls)
        assertEquals(RecordingState.Transcribing, h.controller.state)
        h.transcriber.result.complete(Result.success(""))
    }

    @Test
    fun `blank transcription returns to idle without committing`() = runTest {
        val h = Harness(this)
        h.controller.toggle()
        h.controller.toggle()

        h.transcriber.result.complete(Result.success("  "))
        advanceUntilIdle()

        assertTrue(h.committed.isEmpty())
        assertEquals(RecordingState.Idle, h.controller.state)
    }

    @Test
    fun `transcription failure shows error then recovers to idle`() = runTest {
        val h = Harness(this)
        h.controller.toggle()
        h.controller.toggle()

        h.transcriber.result.complete(Result.failure(IOException("HTTP 401")))
        runCurrent()
        assertTrue(h.controller.state is RecordingState.Error)

        advanceTimeBy(ERROR_DISPLAY_MILLIS + 1)
        assertEquals(RecordingState.Idle, h.controller.state)
        assertTrue(h.committed.isEmpty())
    }

    @Test
    fun `recorder start failure moves to error`() = runTest {
        val h = Harness(this)
        h.recorder.startError = IllegalStateException("mic busy")

        h.controller.toggle()

        assertTrue(h.controller.state is RecordingState.Error)
    }

    @Test
    fun `missing recording file moves to error without transcribing`() = runTest {
        val h = Harness(this)
        h.recorder.stopResult = null
        h.controller.toggle()

        h.controller.toggle()
        runCurrent()

        assertEquals(0, h.transcriber.calls)
        assertTrue(h.controller.state is RecordingState.Error)
    }

    @Test
    fun `missing api key refuses to record`() = runTest {
        val h = Harness(this, apiKeyConfigured = false)

        h.controller.toggle()

        assertFalse(h.recorder.started)
        assertTrue(h.controller.state is RecordingState.Error)
    }

    @Test
    fun `missing mic permission refuses to record`() = runTest {
        val h = Harness(this, micPermission = false)

        h.controller.toggle()

        assertFalse(h.recorder.started)
        assertTrue(h.controller.state is RecordingState.Error)
    }

    @Test
    fun `cancel while recording discards audio and returns to idle`() = runTest {
        val h = Harness(this)
        h.controller.toggle()

        h.controller.cancel()

        assertTrue(h.recorder.cancelled)
        assertEquals(0, h.transcriber.calls)
        assertEquals(RecordingState.Idle, h.controller.state)
    }

    @Test
    fun `state changes are reported to listener`() = runTest {
        val h = Harness(this)

        h.controller.toggle()
        h.controller.toggle()
        h.transcriber.result.complete(Result.success("テスト"))
        advanceUntilIdle()

        assertEquals(
            listOf(RecordingState.Recording, RecordingState.Transcribing, RecordingState.Idle),
            h.states,
        )
    }
}
