package com.kazumaproject.markdownhelperkeyboard.groq_voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

interface VoiceRecorder {
    fun start(): File
    fun stop(): File?
    fun cancel()
}

class GroqAudioRecorder(private val context: Context) : VoiceRecorder {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    override fun start(): File {
        val file = File(context.cacheDir, "groq_voice_recording.m4a")
        val mediaRecorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        outputFile = file
        return file
    }

    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    override fun stop(): File? {
        val file = outputFile
        try {
            recorder?.apply {
                stop()
                release()
            }
        } finally {
            recorder = null
            outputFile = null
        }
        return file
    }

    override fun cancel() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: RuntimeException) {
            recorder?.release()
        } finally {
            recorder = null
            outputFile?.delete()
            outputFile = null
        }
    }
}
