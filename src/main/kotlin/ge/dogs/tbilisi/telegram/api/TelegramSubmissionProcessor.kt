package ge.dogs.tbilisi.telegram.api

import ge.dogs.tbilisi.ai.AiPhotoAnalysisService
import ge.dogs.tbilisi.photos.api.PhotoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TelegramSubmissionProcessor(
    private val telegramIntakeRepository: TelegramIntakeRepository,
    private val photoRepository: PhotoRepository,
    private val telegramBotClient: TelegramBotClient,
    private val aiPhotoAnalysisService: AiPhotoAnalysisService,
    private val telegramBotMessages: TelegramBotMessages,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun process(
        submission: TelegramSubmission,
        language: BotLanguage,
    ): SubmissionProcessingResult {
        telegramIntakeRepository.markSubmissionProcessing(submission.id)

        return runCatching {
            val photoId = "telegram-${submission.id}"
            val imageUrl = telegramBotClient.resolveFileUrl(submission.photoFileId)
                ?: DEFAULT_PLACEHOLDER_IMAGE_URL
            val analysis = aiPhotoAnalysisService.analyzeDogPhoto(imageUrl)

            if (!analysis.isDog) {
                telegramIntakeRepository.markSubmissionFailed(
                    submission.id,
                    "AI could not confirm that the image contains a dog.",
                )
                return SubmissionProcessingResult.Rejected(
                    telegramBotMessages.rejectedNotDog(language),
                )
            }

            val generatedDescription = analysis.description.takeIf { it.isNotBlank() }
                ?: buildFallbackDescription(analysis)

            photoRepository.insertTelegramPhoto(
                id = photoId,
                title = submission.caption?.takeIf { it.isNotBlank() } ?: "Telegram dog photo",
                district = "Tbilisi",
                latitude = submission.latitude,
                longitude = submission.longitude,
                createdAt = submission.createdAt,
                aiDescription = generatedDescription,
                caption = submission.caption.orEmpty(),
                aiConfidence = analysis.confidence,
                source = "TELEGRAM",
                imageUrl = imageUrl,
                status = "PUBLISHED",
                sourceSubmissionId = submission.id,
                telegramPhotoFileId = submission.photoFileId,
                telegramPhotoUniqueId = submission.photoUniqueId,
            )

            telegramIntakeRepository.markSubmissionPublished(submission.id, photoId)
            SubmissionProcessingResult.Published(photoId)
        }.onFailure { error ->
            logger.warn("Failed to process telegram submission {}", submission.id, error)
            telegramIntakeRepository.markSubmissionFailed(
                submission.id,
                error.message ?: "Unknown processing error",
            )
        }.getOrElse { error ->
            SubmissionProcessingResult.Failed(
                error.message ?: "Unknown processing error",
            )
        }
    }

    private companion object {
        const val DEFAULT_PLACEHOLDER_IMAGE_URL =
            "https://placehold.co/1200x900/f4efe5/2f2418?text=Dog+photo+pending"
    }

    private fun buildFallbackDescription(analysis: ge.dogs.tbilisi.ai.DogPhotoAnalysis): String {
        val parts = listOfNotNull(
            analysis.size.takeIf { it != "unknown" }?.let { "$it-sized" },
            analysis.coatColor.takeIf { it.isNotBlank() },
        )

        return if (parts.isEmpty()) {
            "Dog detected in the photo."
        } else {
            "${parts.joinToString(" ")} dog."
        }
    }
}

sealed interface SubmissionProcessingResult {
    data class Published(val photoId: String) : SubmissionProcessingResult
    data class Rejected(val message: String) : SubmissionProcessingResult
    data class Failed(val message: String) : SubmissionProcessingResult
}
