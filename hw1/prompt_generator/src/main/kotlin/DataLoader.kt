package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

interface DataLoader {
    fun loadTemplate(): String
    fun loadData(): List<Map<String, Any>>
}

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

class MockDataLoader(private val template: String, private val data: List<Map<String, String>>) : DataLoader {
    override fun loadTemplate(): String {
        return template
    }

    override fun loadData(): List<Map<String, Any>> {
        return data
    }
}