package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import java.util.*

/**
 * A request for prompt processing containing a unique ID and processed prompt text.
 *
 * @property id Unique identifier (UUID) for tracking the prompt through processing
 * @property prompt The fully resolved prompt text with all placeholders substituted
 */
@Serializable
data class PromptRequest(
    val id: String,      // UUID for tracking
    val prompt: String   // Final prompt with substitutions
)

/**
 * Exception thrown when a template placeholder lacks corresponding data.
 *
 * @property key The placeholder key that couldn't be resolved
 */
class MissingValueException(key: String) :
    IllegalArgumentException("Missing value for placeholder '$key'")

/**
 * Interface for generating prompts from templates and data.
 *
 * Implementations should:
 * - Handle placeholder substitution in templates
 * - Generate unique IDs for each prompt
 * - Support nested data access (e.g., {user.name})
 * - Validate data completeness
 */
interface PromptGenerator {
    /**
     * Generates a list of prompts from a template and dataset.
     *
     * @param template String containing placeholders in {key} format
     * @param data List of maps providing values for placeholders.
     *             Each map represents one prompt variant.
     * @return List of [PromptRequest] objects with:
     *         - Unique UUIDs
     *         - Fully resolved prompts
     * @throws MissingValueException if any placeholder lacks data
     * @throws IllegalArgumentException for malformed templates or data
     */
    fun generatePrompts(template: String, data: List<Map<String, Any>>): List<PromptRequest>
}

/**
 * Standard implementation of [PromptGenerator] with template substitution.
 *
 * Features:
 * - Handles both flat and nested data structures
 * - Properly processes JsonPrimitive values
 * - Generates UUIDs for tracking
 * - Detailed error reporting
 */
class TemplatePromptGenerator : PromptGenerator {
    private val placeholderRegex = "\\{(.+?)}".toRegex()

    /**
     * Generates prompts with comprehensive error handling.
     *
     * Example:
     * ```
     * generatePrompts("Hello {name}", listOf(mapOf("name" to "Alice")))
     * ```
     *
     * @throws MissingValueException if data is incomplete
     * @throws IllegalArgumentException for template/data mismatches
     */
    override fun generatePrompts(
        template: String,
        data: List<Map<String, Any>>
    ): List<PromptRequest> {
        return data.map { dataRow ->
            val filledPrompt = try {
                replacePlaceholders(template, dataRow)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Failed to process data row $dataRow: ${e.message}"
                )
            }
            PromptRequest(
                id = UUID.randomUUID().toString(),
                prompt = filledPrompt
            )
        }
    }

    /**
     * Replaces all placeholders in a template with actual values.
     *
     * @param template String containing placeholders
     * @param data Map of available values
     * @return String with all placeholders resolved
     */
    private fun replacePlaceholders(
        template: String,
        data: Map<String, Any>
    ): String {
        return placeholderRegex.replace(template) { match ->
            val key = match.groupValues[1]
            resolveNestedKey(key, data).toString()
        }
    }

    /**
     * Resolves nested keys (e.g., "user.profile.name") from data.
     *
     * @param key The full key path (may contain dots)
     * @param data The root data map
     * @return The resolved value
     * @throws MissingValueException if any path segment is missing
     */
    private fun resolveNestedKey(key: String, data: Map<String, Any>): Any {
        return key.split('.').fold(data as Any) { current, part ->
            when (current) {
                is Map<*, *> -> {
                    val value = current[part] ?: throw MissingValueException(key)
                    // Handle JsonPrimitive values properly
                    if (value is JsonPrimitive) {
                        value.content // This gets the raw value without quotes
                    } else {
                        value
                    }
                }

                else -> throw IllegalArgumentException(
                    "Nested access failed at '$part' in key '$key'"
                )
            }
        }
    }
}

