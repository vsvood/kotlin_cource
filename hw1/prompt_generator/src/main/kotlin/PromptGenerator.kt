package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import java.util.*

@Serializable
data class PromptRequest(
    val id: String,      // UUID for tracking
    val prompt: String   // Final prompt with substitutions
)

class MissingValueException(key: String) :
    IllegalArgumentException("Missing value for placeholder '$key'")

interface PromptGenerator {
    /**
     * Generates prompts from a template and dataset.
     * @param template String with placeholders like "{key}"
     * @param data List of maps containing values for placeholders
     * @return List of PromptRequest objects with unique IDs
     * @throws MissingValueException if a placeholder has no corresponding data
     */
    fun generatePrompts(template: String, data: List<Map<String, Any>>): List<PromptRequest>
}

class TemplatePromptGenerator : PromptGenerator {
    private val placeholderRegex = "\\{(.+?)}".toRegex()

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

    private fun replacePlaceholders(
        template: String,
        data: Map<String, Any>
    ): String {
        return placeholderRegex.replace(template) { match ->
            val key = match.groupValues[1]
            resolveNestedKey(key, data).toString()
        }
    }

    // For handling nested objects like {user.name}
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

