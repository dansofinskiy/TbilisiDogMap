package ge.dogs.tbilisi.telegram.api

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TelegramIntakeService(
    private val telegramIntakeRepository: TelegramIntakeRepository,
    private val telegramSubmissionProcessor: TelegramSubmissionProcessor,
    private val telegramBotClient: TelegramBotClient,
    private val telegramBotMessages: TelegramBotMessages,
) {
    @Transactional
    fun handleMessage(message: TelegramMessage) {
        val chatId = message.chat.id
        val from = message.from ?: return
        val language = telegramBotMessages.resolve(from.languageCode)

        if (message.text == "/start") {
            telegramBotClient.sendMessage(
                chatId = chatId,
                text = telegramBotMessages.start(language),
            )
            return
        }

        val currentDraft = telegramIntakeRepository.findDraft(chatId)
        val photo = message.photo?.maxByOrNull { it.fileSize ?: 0 }
        val location = message.location

        val updatedDraft = when {
            photo != null -> {
                telegramIntakeRepository.upsertDraft(
                    TelegramSubmissionDraft(
                        chatId = chatId,
                        telegramUserId = from.id,
                        username = from.username,
                        photoFileId = photo.fileId,
                        photoUniqueId = photo.fileUniqueId,
                        caption = message.caption ?: currentDraft?.caption,
                        latitude = currentDraft?.latitude,
                        longitude = currentDraft?.longitude,
                        updatedAt = Instant.now(),
                    ),
                )
            }

            location != null -> {
                telegramIntakeRepository.upsertDraft(
                    TelegramSubmissionDraft(
                        chatId = chatId,
                        telegramUserId = from.id,
                        username = from.username,
                        photoFileId = currentDraft?.photoFileId,
                        photoUniqueId = currentDraft?.photoUniqueId,
                        caption = currentDraft?.caption,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        updatedAt = Instant.now(),
                    ),
                )
            }

            else -> {
                telegramBotClient.sendMessage(
                    chatId = chatId,
                    text = telegramBotMessages.sendPhotoOrLocation(language),
                )
                return
            }
        }

        when {
            updatedDraft.photoFileId == null -> {
                telegramBotClient.sendMessage(
                    chatId = chatId,
                    text = telegramBotMessages.requestPhoto(language),
                )
            }

            updatedDraft.latitude == null || updatedDraft.longitude == null -> {
                telegramBotClient.sendMessage(
                    chatId = chatId,
                    text = telegramBotMessages.requestLocation(language),
                )
            }

            else -> {
                val submission = TelegramSubmission(
                    id = UUID.randomUUID().toString(),
                    chatId = updatedDraft.chatId,
                    telegramUserId = updatedDraft.telegramUserId,
                    username = updatedDraft.username,
                    photoFileId = updatedDraft.photoFileId,
                    photoUniqueId = updatedDraft.photoUniqueId,
                    caption = updatedDraft.caption,
                    latitude = updatedDraft.latitude,
                    longitude = updatedDraft.longitude,
                    status = "NEW",
                    createdAt = Instant.now(),
                )
                telegramIntakeRepository.insertSubmission(submission)
                telegramIntakeRepository.deleteDraft(chatId)
                when (val result = telegramSubmissionProcessor.process(submission, language)) {
                    is SubmissionProcessingResult.Published -> {
                        telegramBotClient.sendMessage(
                            chatId = chatId,
                            text = telegramBotMessages.published(language),
                        )
                    }

                    is SubmissionProcessingResult.Rejected -> {
                        telegramBotClient.sendMessage(
                            chatId = chatId,
                            text = result.message,
                        )
                    }

                    is SubmissionProcessingResult.Failed -> {
                        telegramBotClient.sendMessage(
                            chatId = chatId,
                            text = telegramBotMessages.analysisFailed(language),
                        )
                    }
                }
            }
        }
    }
}
