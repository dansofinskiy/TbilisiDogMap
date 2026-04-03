package ge.dogs.tbilisi.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class AiPhotoAnalysisService(
    @Value("\${openai.api-key:}") private val apiKey: String,
    @Value("\${openai.model:gpt-4.1-mini}") private val model: String,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun analyzeDogPhoto(imageUrl: String): DogPhotoAnalysis {
        if (apiKey.isBlank()) {
            logger.info("Skipping AI photo analysis because openai.api-key is not configured")
            return DogPhotoAnalysis.unavailable()
        }

        val request = OpenAiResponseRequest(
            model = model,
            input = listOf(
                OpenAiInputMessage(
                    role = "user",
                    content = listOf(
                        OpenAiInputText(
                            text = """
                                Analyze the photo and return JSON only.
                                Determine whether the image clearly contains a dog.
                                If it does, describe only the dog's visible coat colors and approximate size.
                                Do not guess the breed.
                                Use this JSON schema:
                                {
                                  "isDog": boolean,
                                  "confidence": number,
                                  "size": "small" | "medium" | "large" | "unknown",
                                  "coatColor": string,
                                  "description": string
                                }
                                If the image is not a dog, return:
                                {
                                  "isDog": false,
                                  "confidence": <number>,
                                  "size": "unknown",
                                  "coatColor": "",
                                  "description": ""
                                }
                            """.trimIndent(),
                        ),
                        OpenAiInputImage(imageUrl = imageUrl, detail = "low"),
                    ),
                ),
            ),
            text = OpenAiTextConfig(
                format = OpenAiJsonObjectFormat(type = "json_object"),
            ),
        )

        val response = RestClient.create("https://api.openai.com")
            .post()
            .uri("/v1/responses")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $apiKey")
            .body(request)
            .retrieve()
            .body(OpenAiResponse::class.java)
            ?: error("OpenAI returned an empty response")

        val payload = response.outputText?.takeIf { it.isNotBlank() }
            ?: response.output.orEmpty()
                .asSequence()
                .flatMap { it.content.orEmpty().asSequence() }
                .firstOrNull { it.type == "output_text" }
                ?.text
            ?: error("OpenAI response did not contain text output")

        val parsed = objectMapper.readValue(payload, DogPhotoAnalysisPayload::class.java)
        return DogPhotoAnalysis(
            isDog = parsed.isDog,
            confidence = parsed.confidence.coerceIn(0.0, 1.0),
            size = parsed.size.ifBlank { "unknown" },
            coatColor = parsed.coatColor.trim(),
            description = parsed.description.trim(),
            aiAvailable = true,
        )
    }
}

data class DogPhotoAnalysis(
    val isDog: Boolean,
    val confidence: Double,
    val size: String,
    val coatColor: String,
    val description: String,
    val aiAvailable: Boolean,
) {
    companion object {
        fun unavailable(): DogPhotoAnalysis = DogPhotoAnalysis(
            isDog = true,
            confidence = 0.0,
            size = "unknown",
            coatColor = "",
            description = "AI description pending.",
            aiAvailable = false,
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DogPhotoAnalysisPayload(
    @JsonProperty("isDog")
    val isDog: Boolean = false,
    val confidence: Double = 0.0,
    val size: String = "unknown",
    val coatColor: String = "",
    val description: String = "",
)

data class OpenAiResponseRequest(
    val model: String,
    val input: List<OpenAiInputMessage>,
    val text: OpenAiTextConfig,
)

data class OpenAiInputMessage(
    val role: String,
    val content: List<OpenAiInputContent>,
)

sealed interface OpenAiInputContent

data class OpenAiInputText(
    val type: String = "input_text",
    val text: String,
) : OpenAiInputContent

data class OpenAiInputImage(
    val type: String = "input_image",
    @JsonProperty("image_url")
    val imageUrl: String,
    val detail: String = "auto",
) : OpenAiInputContent

data class OpenAiTextConfig(
    val format: OpenAiJsonObjectFormat,
)

data class OpenAiJsonObjectFormat(
    val type: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiResponse(
    @JsonProperty("output_text")
    val outputText: String? = null,
    val output: List<OpenAiOutputItem>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiOutputItem(
    val content: List<OpenAiOutputContent>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiOutputContent(
    val type: String? = null,
    val text: String? = null,
)
