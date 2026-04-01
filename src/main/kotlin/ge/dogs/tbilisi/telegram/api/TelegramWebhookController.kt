package ge.dogs.tbilisi.telegram.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/telegram")
class TelegramWebhookController(
    private val telegramWebhookService: TelegramWebhookService,
) {
    @PostMapping("/webhook")
    fun handleUpdate(@RequestBody update: TelegramUpdate): ResponseEntity<Void> {
        telegramWebhookService.handleUpdate(update)
        return ResponseEntity.ok().build()
    }
}
