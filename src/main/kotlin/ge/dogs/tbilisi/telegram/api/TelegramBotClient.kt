package ge.dogs.tbilisi.telegram.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class TelegramBotClient(
    @Value("\${telegram.bot.token:}") private val botToken: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val normalizedBotToken: String
        get() = botToken.trim()

    fun sendMessage(chatId: Long, text: String) {
        if (normalizedBotToken.isBlank()) {
            logger.info("Skipping Telegram reply because telegram.bot.token is not configured")
            return
        }

        runCatching {
            RestClient.create()
                .post()
                .uri("https://api.telegram.org/bot${normalizedBotToken}/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("chat_id" to chatId, "text" to text))
                .retrieve()
                .toBodilessEntity()
        }.onFailure { error ->
            logger.warn("Failed to send Telegram message", error)
        }
    }

    fun resolveFileUrl(fileId: String): String? {
        if (normalizedBotToken.isBlank()) {
            logger.warn("Cannot resolve Telegram file URL because telegram.bot.token is not configured")
            return null
        }

        return runCatching {
            val response = RestClient.create()
                .get()
                .uri("https://api.telegram.org/bot${normalizedBotToken}/getFile?file_id={fileId}", fileId)
                .retrieve()
                .body(TelegramFileResponse::class.java)

            response?.result?.filePath?.let { filePath ->
                "https://api.telegram.org/file/bot${normalizedBotToken}/$filePath"
            }
        }.onFailure { error ->
            logger.warn("Failed to resolve Telegram file URL", error)
        }.getOrNull()
    }
}
