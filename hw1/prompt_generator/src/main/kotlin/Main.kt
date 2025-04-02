package org.example

import kotlinx.coroutines.*

fun main() {

    val test = ProcessingPipelineTest(
        JsonDataLoader("/data.json"),
        TemplatePromptGenerator(),
        HuggingFacePromptProcessor("hugging_face token")
    )

    runBlocking {
        val job = launch {
            test.run()
        }

        job.join() // Wait for the coroutine to complete
        println("Done")
    }

}