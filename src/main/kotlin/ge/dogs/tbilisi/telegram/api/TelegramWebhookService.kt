package ge.dogs.tbilisi.telegram.api

import org.springframework.stereotype.Service

@Service
class TelegramWebhookService(
    private val telegramIntakeService: TelegramIntakeService,
) {
    fun handleUpdate(update: TelegramUpdate) {
        val message = update.message ?: return
        telegramIntakeService.handleMessage(message)
    }
}
