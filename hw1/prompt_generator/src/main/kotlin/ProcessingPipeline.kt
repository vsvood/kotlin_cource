package org.example

/**
 * Interface defining a pipeline for processing prompts through a complete workflow.
 *
 * The pipeline coordinates three main operations:
 * 1. Loading templates and data via [DataLoader]
 * 2. Generating prompts via [PromptGenerator]
 * 3. Processing prompts via [PromptProcessor]
 *
 * Implementations should handle the full lifecycle from template loading to response collection.
 */
interface ProcessingPipeline {
    /**
     * Executes the complete prompt processing pipeline.
     *
     * @return Map of response IDs to their corresponding generated content,
     *         where keys are the UUIDs from [PromptRequest] objects
     * @throws MissingValueException if template placeholders lack matching data
     * @throws PromptProcessingException if prompt processing fails
     * @throws IllegalArgumentException if input data is invalid
     */
    suspend fun run(): Map<String, String>
}

/**
 * Standard production implementation of [ProcessingPipeline].
 *
 * This implementation silently processes prompts without logging, suitable for production use.
 *
 * @property dataLoader Component for loading templates and data
 * @property promptGenerator Component for generating prompts from templates
 * @property promptProcessor Component for processing generated prompts
 */
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

/**
 * Debugging implementation of [ProcessingPipeline] with verbose logging.
 *
 * This implementation logs each pipeline stage for debugging and testing purposes.
 * It maintains the same operational flow as [ProcessingPipelineImpl] but with added visibility.
 *
 * @property dataLoader Component for loading templates and data
 * @property promptGenerator Component for generating prompts from templates
 * @property promptProcessor Component for processing generated prompts
 */
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
            println("\tprompt: ${prompt.prompt}\n")
        }
        val responses = promptProcessor.processPrompts(prompts)
        println("responses:")
        responses.forEach{ response ->
            println("\tuuid: ${response.key}")
            println("\tresponse: ${response.value}\n")
        }
        return responses
    }
}