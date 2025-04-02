package org.example

import kotlinx.coroutines.*

fun main() {

    val test = ProcessingPipelineTest(
        JsonDataLoader("/data.json"),
        TemplatePromptGenerator(),
        HuggingFacePromptProcessor("hugging_face token")
    )

    runBlocking {
        test.run()
    }

}