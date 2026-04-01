package ge.dogs.tbilisi.telegram.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TelegramUpdate(
    @JsonProperty("update_id")
    val updateId: Long? = null,
    val message: TelegramMessage? = null,
)

data class TelegramMessage(
    @JsonProperty("message_id")
    val messageId: Long? = null,
    val text: String? = null,
    val caption: String? = null,
    val photo: List<TelegramPhotoSize>? = null,
    val location: TelegramLocation? = null,
    val chat: TelegramChat,
    val from: TelegramUser? = null,
)

data class TelegramChat(
    val id: Long,
)

data class TelegramUser(
    val id: Long,
    val username: String? = null,
)

data class TelegramPhotoSize(
    @JsonProperty("file_id")
    val fileId: String,
    @JsonProperty("file_unique_id")
    val fileUniqueId: String? = null,
    @JsonProperty("file_size")
    val fileSize: Int? = null,
)

data class TelegramLocation(
    val latitude: Double,
    val longitude: Double,
)

data class TelegramSubmissionDraft(
    val chatId: Long,
    val telegramUserId: Long,
    val username: String?,
    val photoFileId: String?,
    val photoUniqueId: String?,
    val caption: String?,
    val latitude: Double?,
    val longitude: Double?,
    val updatedAt: Instant,
)

data class TelegramSubmission(
    val id: String,
    val chatId: Long,
    val telegramUserId: Long,
    val username: String?,
    val photoFileId: String,
    val photoUniqueId: String?,
    val caption: String?,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: Instant,
)

data class TelegramFileResponse(
    val ok: Boolean,
    val result: TelegramFileResult? = null,
)

data class TelegramFileResult(
    @JsonProperty("file_path")
    val filePath: String? = null,
)
