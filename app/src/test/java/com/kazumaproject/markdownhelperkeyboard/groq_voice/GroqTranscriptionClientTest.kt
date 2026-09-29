package com.kazumaproject.markdownhelperkeyboard.groq_voice

import java.io.File
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GroqTranscriptionClientTest {

    private lateinit var server: MockWebServer
    private lateinit var audioFile: File

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        audioFile = File.createTempFile("recording", ".m4a").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
        audioFile.delete()
    }

    @Test
    fun `sends multipart request with model, language and auth header`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"text": "こんにちは"}"""))
        val client = GroqTranscriptionClient(
            apiKey = "test-api-key",
            baseUrl = server.url("/").toString(),
        )

        client.transcribe(audioFile)

        val request = server.takeRequest()
        assertEquals("Bearer test-api-key", request.getHeader("Authorization"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("whisper-large-v3-turbo"))
        assertTrue(body.contains("name=\"language\"") && body.contains("ja"))
    }

    @Test
    fun `returns parsed text on success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"text": "こんにちは"}"""))
        val client = GroqTranscriptionClient(
            apiKey = "test-api-key",
            baseUrl = server.url("/").toString(),
        )

        val result = client.transcribe(audioFile)

        assertEquals("こんにちは", result.getOrNull())
    }

    @Test
    fun `returns failure on error response`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error": "invalid api key"}"""))
        val client = GroqTranscriptionClient(
            apiKey = "bad-key",
            baseUrl = server.url("/").toString(),
        )

        val result = client.transcribe(audioFile)

        assertTrue(result.isFailure)
    }
}
