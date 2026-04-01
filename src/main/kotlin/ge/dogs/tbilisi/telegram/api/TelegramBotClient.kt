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

    fun sendMessage(chatId: Long, text: String) {
        if (botToken.isBlank()) {
            logger.info("Skipping Telegram reply because telegram.bot.token is not configured")
            return
        }

        runCatching {
            RestClient.create("https://api.telegram.org")
                .post()
                .uri("/bot{token}/sendMessage", botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("chat_id" to chatId, "text" to text))
                .retrieve()
                .toBodilessEntity()
        }.onFailure { error ->
            logger.warn("Failed to send Telegram message", error)
        }
    }

    fun resolveFileUrl(fileId: String): String? {
        if (botToken.isBlank()) {
            logger.warn("Cannot resolve Telegram file URL because telegram.bot.token is not configured")
            return null
        }

        return runCatching {
            val response = RestClient.create("https://api.telegram.org")
                .get()
                .uri("/bot{token}/getFile?file_id={fileId}", botToken, fileId)
                .retrieve()
                .body(TelegramFileResponse::class.java)

            response?.result?.filePath?.let { filePath ->
                "https://api.telegram.org/file/bot$botToken/$filePath"
            }
        }.onFailure { error ->
            logger.warn("Failed to resolve Telegram file URL", error)
        }.getOrNull()
    }
}
