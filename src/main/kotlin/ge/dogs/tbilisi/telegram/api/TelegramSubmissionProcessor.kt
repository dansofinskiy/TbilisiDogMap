package ge.dogs.tbilisi.telegram.api

import ge.dogs.tbilisi.photos.api.PhotoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TelegramSubmissionProcessor(
    private val telegramIntakeRepository: TelegramIntakeRepository,
    private val photoRepository: PhotoRepository,
    private val telegramBotClient: TelegramBotClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun process(submission: TelegramSubmission) {
        telegramIntakeRepository.markSubmissionProcessing(submission.id)

        runCatching {
            val photoId = "telegram-${submission.id}"
            val imageUrl = telegramBotClient.resolveFileUrl(submission.photoFileId)
                ?: DEFAULT_PLACEHOLDER_IMAGE_URL

            photoRepository.insertTelegramPhoto(
                id = photoId,
                title = submission.caption?.takeIf { it.isNotBlank() } ?: "Telegram dog photo",
                district = "Tbilisi",
                latitude = submission.latitude,
                longitude = submission.longitude,
                createdAt = submission.createdAt,
                aiDescription = "AI description pending.",
                caption = submission.caption.orEmpty(),
                aiConfidence = 0.0,
                source = "TELEGRAM",
                imageUrl = imageUrl,
                status = "PUBLISHED",
                sourceSubmissionId = submission.id,
                telegramPhotoFileId = submission.photoFileId,
                telegramPhotoUniqueId = submission.photoUniqueId,
            )

            telegramIntakeRepository.markSubmissionPublished(submission.id, photoId)
        }.onFailure { error ->
            logger.warn("Failed to process telegram submission {}", submission.id, error)
            telegramIntakeRepository.markSubmissionFailed(
                submission.id,
                error.message ?: "Unknown processing error",
            )
            throw error
        }
    }

    private companion object {
        const val DEFAULT_PLACEHOLDER_IMAGE_URL =
            "https://placehold.co/1200x900/f4efe5/2f2418?text=Dog+photo+pending"
    }
}
