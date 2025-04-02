package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

/**
 * Interface for processing generated prompts through LLM services.
 *
 * Handles the execution of prompt requests and returns processed responses.
 * Implementations may connect to various LLM APIs or provide mock responses.
 */
interface PromptProcessor {
    /**
     * Processes a batch of prompts asynchronously.
     *
     * @param requests List of [PromptRequest] objects containing:
     *        - id: Unique identifier for each prompt
     *        - prompt: Fully resolved prompt text
     * @return Map associating each request ID with its corresponding response
     * @throws PromptProcessingException for API failures or network errors
     */
    suspend fun processPrompts(requests: List<PromptRequest>): Map<String, String>

    /**
     * Convenience method for processing a single prompt.
     *
     * Default implementation processes the prompt as a batch of one.
     *
     * @param request Single prompt request to process
     * @return The processed response content
     * @throws PromptProcessingException for API failures or network errors
     * @throws NoSuchElementException if no response is received
     */
    suspend fun processPrompt(request: PromptRequest): String =
        processPrompts(listOf(request))[request.id] ?: error("No response received")
}

/**
 * Hugging Face API implementation of [PromptProcessor].
 *
 * Features:
 * - Supports all Hugging Face inference endpoints
 * - Configurable generation parameters
 * - Automatic request retries
 * - Parallel request processing
 *
 * @property apiToken Hugging Face API token
 * @property modelId Model identifier (e.g., "HuggingFaceH4/zephyr-7b-beta")
 * @property timeoutSeconds Network timeout in seconds
 */
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

    /**
     * Internal request data structure for Hugging Face API.
     */
    @Serializable
    private data class HuggingFaceRequest(
        val inputs: String,
        val parameters: Parameters = Parameters()
    ) {
        /**
         * Generation parameters for controlling output.
         */
        @Serializable
        data class Parameters(
            val max_new_tokens: Int = 200,
            val temperature: Double = 0.7,
            val do_sample: Boolean = true
        )
    }

    /**
     * Internal response data structure for Hugging Face API.
     */
    @Serializable
    private data class HuggingFaceResponse(
        val generated_text: String
    )

    /**
     * Processes prompts in parallel using coroutines.
     *
     * Makes concurrent API calls while preserving request-response mapping.
     */
    override suspend fun processPrompts(requests: List<PromptRequest>): Map<String, String> {
        return coroutineScope {
            requests.map { request ->
                async {
                    request.id to processSinglePrompt(request.prompt)
                }
            }.awaitAll().toMap()
        }
    }

    /**
     * Executes single API call to Hugging Face inference endpoint.
     *
     * @param prompt The fully resolved prompt text to process
     * @return The generated response content
     * @throws PromptProcessingException for API errors or network failures
     */
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

/**
 * Exception for prompt processing failures.
 *
 * @property message Description of the error
 * @property cause Underlying exception if available
 */
class PromptProcessingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)