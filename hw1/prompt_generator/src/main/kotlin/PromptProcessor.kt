package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

interface PromptProcessor {
    /**
     * Processes generated prompts and returns responses
     * @param requests List of prompt requests with unique IDs
     * @return Map of response IDs to their content
     */
    suspend fun processPrompts(requests: List<PromptRequest>): Map<String, String>

    /**
     * Optional: Process single prompt (default implementation provided)
     */
    suspend fun processPrompt(request: PromptRequest): String =
        processPrompts(listOf(request))[request.id] ?: error("No response received")
}

class HuggingFacePromptProcessor(
    private val apiToken: String,
    private val modelId: String = "HuggingFaceH4/zephyr-7b-beta",
    private val timeoutSeconds: Long = 30
) : PromptProcessor {

    private val jsonFormat = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .build()

    @Serializable
    private data class HuggingFaceRequest(
        val inputs: String,
        val parameters: Parameters = Parameters()
    ) {
        @Serializable
        data class Parameters(
            val max_new_tokens: Int = 200,
            val temperature: Double = 0.7,
            val do_sample: Boolean = true
        )
    }

    @Serializable
    private data class HuggingFaceResponse(
        val generated_text: String
    )

    override suspend fun processPrompts(requests: List<PromptRequest>): Map<String, String> {
        return coroutineScope {
            requests.map { request ->
                async {
                    request.id to processSinglePrompt(request.prompt)
                }
            }.awaitAll().toMap()
        }
    }

    private fun processSinglePrompt(prompt: String): String {
        val requestBody = jsonFormat.encodeToString(
            HuggingFaceRequest(inputs = prompt)
        )

        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/$modelId")
            .addHeader("Authorization", "Bearer $apiToken")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw PromptProcessingException(
                        "API request failed: ${response.code} - ${response.message}"
                    )
                }

                val responseBody = response.body?.string()
                    ?: throw PromptProcessingException("Empty response body")

                // Handle array response (some models return array)
                if (responseBody.startsWith('[')) {
                    jsonFormat.decodeFromString<List<HuggingFaceResponse>>(responseBody)
                        .firstOrNull()?.generated_text?.trim()
                        ?: throw PromptProcessingException("Empty generated text")
                } else {
                    jsonFormat.decodeFromString<HuggingFaceResponse>(responseBody)
                        .generated_text.trim()
                }
            }
        } catch (e: IOException) {
            throw PromptProcessingException("Network error", e)
        }
    }
}

class PromptProcessingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)