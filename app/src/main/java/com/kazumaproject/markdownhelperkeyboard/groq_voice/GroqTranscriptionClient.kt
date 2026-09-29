package com.kazumaproject.markdownhelperkeyboard.groq_voice

import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface TranscriptionClient {
    suspend fun transcribe(audioFile: File): Result<String>
}

class GroqTranscriptionClient(
    private val apiKey: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val baseUrl: String = "https://api.groq.com/openai/v1/audio/transcriptions",
    private val model: String = "whisper-large-v3-turbo",
    private val language: String = "ja",
) : TranscriptionClient {

    override suspend fun transcribe(audioFile: File): Result<String> {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", model)
            .addFormDataPart("language", language)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/m4a".toMediaType()),
            )
            .build()

        val request = Request.Builder()
            .url(baseUrl)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            runCatching {
                val response = executeAsync(request)
                response.use {
                    if (!it.isSuccessful) {
                        throw IOException("Groq API error: HTTP ${it.code}")
                    }
                    val body = it.body?.string().orEmpty()
                    JSONObject(body).getString("text")
                }
            }
        }
    }

    private suspend fun executeAsync(request: Request): Response =
        suspendCoroutine { continuation ->
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }
            })
        }
}
