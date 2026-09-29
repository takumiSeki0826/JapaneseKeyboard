package com.kazumaproject.markdownhelperkeyboard.groq_voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kazumaproject.markdownhelperkeyboard.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val START_TONE_DURATION_MS = 300L

/**
 * Android-side wiring for [VoiceRecordingController]: start tone, error toasts and
 * permission / API key checks. Keeps the IMEService changes to a few call sites.
 */
class GroqVoiceInput(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onStateChanged: (RecordingState) -> Unit,
    onTranscribed: (String) -> Unit,
) {
    private var toneGenerator: ToneGenerator? = null

    private val controller = VoiceRecordingController(
        recorder = GroqAudioRecorder(context),
        transcriber = GroqTranscriptionClient(apiKey = BuildConfig.GROQ_API_KEY),
        scope = scope,
        isApiKeyConfigured = { BuildConfig.GROQ_API_KEY.isNotBlank() },
        hasMicPermission = {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        },
        onStateChanged = ::handleStateChanged,
        onTranscribed = onTranscribed,
    )

    val isRecording: Boolean
        get() = controller.state is RecordingState.Recording

    /** True when the next [toggle] will start a new recording. */
    val isReadyToRecord: Boolean
        get() = controller.state is RecordingState.Idle || controller.state is RecordingState.Error

    fun toggle() = controller.toggle()

    fun cancel() = controller.cancel()

    fun release() {
        controller.cancel()
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun handleStateChanged(state: RecordingState) {
        when (state) {
            is RecordingState.Recording -> playStartTone()
            is RecordingState.Error ->
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()

            else -> Unit
        }
        onStateChanged(state)
    }

    private fun playStartTone() {
        // Keep a strong reference until the tone has played; releasing it immediately
        // cuts the tone off before it becomes audible. STREAM_MUSIC so the cue is still
        // heard when the phone is on silent/vibrate (notification volume 0).
        toneGenerator?.release()
        val generator = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)
        } catch (_: RuntimeException) {
            return
        }
        toneGenerator = generator
        generator.startTone(ToneGenerator.TONE_PROP_BEEP)
        scope.launch {
            delay(START_TONE_DURATION_MS)
            generator.release()
            if (toneGenerator === generator) toneGenerator = null
        }
    }
}
