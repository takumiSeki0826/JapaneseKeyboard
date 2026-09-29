package com.kazumaproject.markdownhelperkeyboard.groq_voice

import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val ERROR_DISPLAY_MILLIS = 3_000L

/**
 * Tap-to-start / tap-to-stop voice input backed by Groq Whisper.
 * The IME only calls [toggle] and [cancel]; results are delivered via [onTranscribed].
 */
class VoiceRecordingController(
    private val recorder: VoiceRecorder,
    private val transcriber: TranscriptionClient,
    private val scope: CoroutineScope,
    private val isApiKeyConfigured: () -> Boolean,
    private val hasMicPermission: () -> Boolean,
    private val onStateChanged: (RecordingState) -> Unit,
    private val onTranscribed: (String) -> Unit,
) {
    var state: RecordingState = RecordingState.Idle
        private set

    private var transcriptionJob: Job? = null
    private var errorRecoveryJob: Job? = null

    fun toggle() {
        when (state) {
            is RecordingState.Idle, is RecordingState.Error -> startRecording()
            is RecordingState.Recording -> stopAndTranscribe()
            is RecordingState.Transcribing -> Unit
        }
    }

    fun cancel() {
        when (state) {
            is RecordingState.Recording -> {
                recorder.cancel()
                setState(RecordingState.Idle)
            }

            is RecordingState.Transcribing -> {
                transcriptionJob?.cancel()
                setState(RecordingState.Idle)
            }

            else -> Unit
        }
    }

    private fun startRecording() {
        errorRecoveryJob?.cancel()
        if (!isApiKeyConfigured()) {
            fail("Groq APIキーが設定されていません")
            return
        }
        if (!hasMicPermission()) {
            fail("マイク権限をアプリの設定画面から許可してください")
            return
        }
        try {
            recorder.start()
        } catch (e: Exception) {
            recorder.cancel()
            fail("録音を開始できませんでした")
            return
        }
        transition(RecordingEvent.MicTapped)
    }

    private fun stopAndTranscribe() {
        val file: File? = try {
            recorder.stop()
        } catch (e: Exception) {
            null
        }
        if (file == null) {
            fail("録音の保存に失敗しました")
            return
        }

        transition(RecordingEvent.MicTapped)
        transcriptionJob = scope.launch {
            transcriber.transcribe(file).fold(
                onSuccess = { text ->
                    val trimmed = text.trim()
                    if (trimmed.isNotEmpty()) onTranscribed(trimmed)
                    transition(RecordingEvent.TranscriptionSucceeded(trimmed))
                },
                onFailure = { fail("文字起こしに失敗しました") },
            )
        }
    }

    private fun fail(message: String) {
        setState(RecordingState.Error(message))
        errorRecoveryJob = scope.launch {
            delay(ERROR_DISPLAY_MILLIS)
            transition(RecordingEvent.ErrorAcknowledged)
        }
    }

    private fun transition(event: RecordingEvent) {
        setState(RecordingStateMachine.reduce(state, event))
    }

    private fun setState(next: RecordingState) {
        state = next
        onStateChanged(next)
    }
}
