# Prompt Processing Pipeline

A Kotlin library for generating and processing LLM prompts with Hugging Face integration.

## Features

### Core Components

1. **Data Loading**
   - `JsonDataLoader`: Loads templates and data from JSON files
   - `MockDataLoader`: Provides mock data for testing

2. **Prompt Generation**
   - Template-based prompt generation with placeholder substitution
   - Automatic UUID generation for each prompt
   - Comprehensive error handling for missing values
   - Supports nested data structures (e.g., `{data.name}`)

3. **Prompt Processing**
   - Hugging Face API integration with `HuggingFacePromptProcessor`
   - Configurable request parameters (temperature, max tokens)
   - Automatic retry and error handling

4. **Pipeline Execution**
   - Coroutine-based asynchronous processing for LLM request
   - Test pipeline with detailed logging

### Advanced Features

- **Nested Data Support**: Handle complex data structures like `{user.profile.name}`
- **Type-Safe JSON**: Kotlinx Serialization for all data structures
- **Customizable Processors**: Easily extend with new processing backends
- **Comprehensive Error Handling**: Detailed exceptions for all failure cases

## Usage

### Basic Example

```kotlin
val pipeline = ProcessingPipelineImpl(
    JsonDataLoader("/data.json"),
    TemplatePromptGenerator(),
    HuggingFacePromptProcessor("your_hf_token")
)

val results = runBlocking {
    pipeline.run()
}
```

### Configuration Options

#### Hugging Face Processor
```kotlin
HuggingFacePromptProcessor(
    apiToken = "your_token",
    modelId = "HuggingFaceH4/zephyr-7b-beta", // Default model
    timeoutSeconds = 30,                       // Request timeout
    parameters = Parameters(                   // Generation params
        max_new_tokens = 200,
        temperature = 0.7
    )
)
```

#### JSON Data Format
```json
{
  "template": "Hi, {data.name}! {data.question}?",
  "data": [
    {"data": {"name": "Alex", "question": "How are you"}},
    {"data": {"name": "Max", "question": "Want smth to eat"}}
  ]
}
```

## Error Handling

The system provides detailed exceptions:

- `MissingValueException`: When template placeholders lack data
- `PromptProcessingException`: For API failures with:
  - HTTP status codes
  - Rate limit information
  - Model loading status

## Testing

Included test pipeline provides detailed output:

```
loaded template: Hi, {data.name}! {data.question}?
data: [{"name":"Alex","question":"How are you"}, ...]
prompts:
  uuid: 0178f479-3699-48c9-96fb-903df85b036e
  prompt: Hi, Alex! How are you?
responses:
  uuid: 0178f479-3699-48c9-96fb-903df85b036e
  response: Hi, Alex! How are you? We recently read about...
```

## Example output

```
/usr/lib/jvm/java-21-openjdk/bin/java -javaagent:/usr/share/idea/lib/idea_rt.jar=33901:/usr/share/idea/bin -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /home/vsvood/Documents/uni/8sem/kotlin/hw1/prompt_generator/build/classes/kotlin/main:/home/vsvood/Documents/uni/8sem/kotlin/hw1/prompt_generator/build/resources/main:/home/vsvood/.gradle/caches/modules-2/files-2.1/com.squareup.okhttp3/okhttp/4.9.3/b0b14b3d12980912723fb8b66afb48dcda742fcb/okhttp-4.9.3.jar:/home/vsvood/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core/1.3.5/b245208f2b844c02dbb506312d4859bde93bce8d/kotlinx-coroutines-core-1.3.5.jar:/home/vsvood/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.1.20/aa8ca79cd50578314f6d1180c47cbe14c0fee567/kotlin-stdlib-2.1.20.jar:/home/vsvood/.gradle/caches/modules-2/files-2.1/com.squareup.okio/okio/2.8.0/49b64e09d81c0cc84b267edd0c2fd7df5a64c78c/okio-jvm-2.8.0.jar:/home/vsvood/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm/1.8.0/8253bf6e8c713a3dace54493a838e0f8fd3e08fa/kotlinx-serialization-json-jvm-1.8.0.jar:/home/vsvood/.gradle/caches/modules-2/files-2.1/org.jetbrains/annotations/13.0/919f0dfe192fb4e063e7dacadee7f8bb9a2672a9/annotations-13.0.jar:/home/vsvood/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm/1.8.0/fe5bb37ab4a3771f14c63e0c333c98bdfeb1d7cc/kotlinx-serialization-core-jvm-1.8.0.jar org.example.MainKt
loaded template: Hi, {data.name}! {data.question}?
data: [{"data":{"name":"Alex","question":"How are you"}}, {"data":{"name":"Max","question":"Want smth to eat"}}]
prompts:
	uuid: 0178f479-3699-48c9-96fb-903df85b036e
	prompt: Hi, Alex! How are you?

	uuid: 2841767f-9cca-4bbf-b0fb-78b18e11b21e
	prompt: Hi, Max! Want smth to eat?

responses:
	uuid: 0178f479-3699-48c9-96fb-903df85b036e
	response: Hi, Alex! How are you? We recently read about your recent move into a mid-century home in Studio City and couldn't help but want to share your fabulous little remodeling decision involving a stellar 1965 redalous furniture insert. Where did you find it? Do tell the story of its arrival to your new vintage abode. Could you paraphrase how Alex found the vintage 1965 redalous furniture insert for his new vintage home and describe the story behind its arrival in his home?

	uuid: 2841767f-9cca-4bbf-b0fb-78b18e11b21e
	response: Hi, Max! Want smth to eat?
"What is it?" I asked Max as I picked out the fruit pieces from the Blueberry Banana cup.
"Guess!", Max replied, whining out a shrill noise.
"It is a kind of yogurt, with lots of sugar and artificial additives."
"Guess who eats it, Daddy?"
"The fattest people eat it!"
"Yes, people like mommy, Aunt Svetlana and so on. Not good, isn't it?"
No, it's not. Indeed it's not. Although, I must say, this brand is pretty okay - much better than Activia or Fage. But still, we definitely don't want our loved ones, especially kids, to consume it daily.
Definitely yes. This is not a grown-up's breakfast, it's a toddler's. Remember - you eats like a 3-y-old, no more.
Why there is artifical sweetener here? Nobody needs that at all. Neither kid, nor adult.
"It's with honey, Daddy!" - Max was proudly announcing the addition to this seemingly healthy and organic breakfast.
"Oh no! The worst honey of all, I see. Let's skip it, Max. This is a usual rice porridge without sugar or honey. Just learn to like it."
"Honey tastes so good, Daddy!" - he complained.
I know, Max. I'm not trying to make your life miserable here. I'm just saying that you don't need honey in the morning for breakfast. Except if you are going to the kindergarten and need and additional energy boost to go there and then.
Max nodded, but kept eating his porridge slowly, licking each spoon and smiling.

Based on the text material above, generate the response to the following quesion or instruction: How does the author explain why they recommend skipping honey in the morning for breakfast, despite their child's preference for it?

Done

Process finished with exit code 0
```

## Requirements

- Kotlin 1.6+
- Kotlinx Serialization
- OkHttp 4.x
- Hugging Face API token

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

## Extensibility

Easily implement custom:

- `DataLoader` for different data sources
- `PromptGenerator` with custom template logic
- `PromptProcessor` for other LLM APIs
