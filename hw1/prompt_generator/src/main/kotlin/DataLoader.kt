package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Interface for loading prompt templates and associated data from various sources.
 *
 * Implementations should provide:
 * - A template string containing placeholders (e.g., "{name}")
 * - A list of data maps to populate the template placeholders
 *
 * @see JsonDataLoader for JSON file implementation
 * @see MockDataLoader for testing with mock data
 */
interface DataLoader {
    fun loadTemplate(): String
    fun loadData(): List<Map<String, Any>>
}

/**
 * Implementation of DataLoader that loads templates and data from a JSON file.
 *
 * Expected JSON format:
 * ```json
 * {
 *   "template": "Hello {name}!",
 *   "data": [
 *     {"name": "Alice"},
 *     {"name": "Bob"}
 *   ]
 * }
 * ```
 *
 * Support nested data:
 * ```
 * {
 *   "template": "Hi, {data.name}! {data.question}?",
 *   "data": [
 *     {
 *       "data" : {
 *         "name": "Alex",
 *         "question": "How are you"
 *       }
 *     },
 *     {
 *       "data" : {
 *         "name": "Max",
 *         "question": "Want smth to eat"
 *       }
 *     }
 *   ]
 * }
 * ```
 *
 * @property filePath Path to the JSON resource file (relative to resources folder)
 * @throws IllegalArgumentException if the file doesn't exist or has invalid format
 */
class JsonDataLoader(private val filePath: String) : DataLoader {
    private val json by lazy {
        Json.decodeFromString<JsonData>(
            this::class.java.getResource(filePath)?.readText()
                ?: throw IllegalArgumentException("no such file $filePath")
        )
    }

    override fun loadTemplate(): String {
        return json.template
    }

    override fun loadData(): List<Map<String, Any>> {
        return json.data
    }

    @Serializable
    private data class JsonData(
        val template: String,
        val data: List<JsonObject>
    )
}

/**
 * Mock implementation of DataLoader for testing purposes.
 *
 * @property template The template string to use for all requests
 * @property data The mock data to use for template substitution
 */
class MockDataLoader(private val template: String, private val data: List<Map<String, String>>) : DataLoader {
    override fun loadTemplate(): String {
        return template
    }

    override fun loadData(): List<Map<String, Any>> {
        return data
    }
}