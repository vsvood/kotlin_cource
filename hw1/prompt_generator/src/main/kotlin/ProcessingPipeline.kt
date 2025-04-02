package org.example

interface ProcessingPipeline {
    suspend fun run(): Map<String, String>
}

class ProcessingPipelineImpl(
    private val dataLoader: DataLoader,
    private val promptGenerator: PromptGenerator,
    private val promptProcessor: PromptProcessor
) : ProcessingPipeline {

    override suspend fun run(): Map<String, String> {
        val template = dataLoader.loadTemplate()
        val data = dataLoader.loadData()
        val prompts = promptGenerator.generatePrompts(template, data)
        val responses = promptProcessor.processPrompts(prompts)
        return responses
    }
}

class ProcessingPipelineTest(
    private val dataLoader: DataLoader,
    private val promptGenerator: PromptGenerator,
    private val promptProcessor: PromptProcessor
) : ProcessingPipeline {

    override suspend fun run(): Map<String, String> {
        val template = dataLoader.loadTemplate()
        println("loaded template: $template")
        val data = dataLoader.loadData()
        println("data: $data")
        val prompts = promptGenerator.generatePrompts(template, data)
        println("prompts:")
        prompts.forEach{ prompt ->
            println("\tuuid: ${prompt.id}")
            println("\tprompt: ${prompt.prompt}")
        }
        val responses = promptProcessor.processPrompts(prompts)
        println("responses:")
        responses.forEach{ response ->
            println("\tuuid: ${response.key}")
            println("\tresponse: ${response.value}")
        }
        return responses
    }
}