package ge.dogs.tbilisi.telegram.api

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

@Repository
class TelegramIntakeRepository(
    private val jdbcClient: JdbcClient,
) {
    fun findDraft(chatId: Long): TelegramSubmissionDraft? = jdbcClient.sql(
        """
        select chat_id, telegram_user_id, username, photo_file_id, photo_unique_id,
               caption, latitude, longitude, updated_at
        from telegram_submission_drafts
        where chat_id = :chatId
        """.trimIndent(),
    )
        .param("chatId", chatId)
        .query(draftRowMapper)
        .optional()
        .orElse(null)

    fun upsertDraft(draft: TelegramSubmissionDraft): TelegramSubmissionDraft {
        jdbcClient.sql(
            """
            insert into telegram_submission_drafts (
                chat_id, telegram_user_id, username, photo_file_id, photo_unique_id,
                caption, latitude, longitude, updated_at
            ) values (
                :chatId, :telegramUserId, :username, :photoFileId, :photoUniqueId,
                :caption, :latitude, :longitude, :updatedAt
            )
            on conflict (chat_id) do update set
                telegram_user_id = excluded.telegram_user_id,
                username = excluded.username,
                photo_file_id = excluded.photo_file_id,
                photo_unique_id = excluded.photo_unique_id,
                caption = excluded.caption,
                latitude = excluded.latitude,
                longitude = excluded.longitude,
                updated_at = excluded.updated_at
            """.trimIndent(),
        )
            .param("chatId", draft.chatId)
            .param("telegramUserId", draft.telegramUserId)
            .param("username", draft.username)
            .param("photoFileId", draft.photoFileId)
            .param("photoUniqueId", draft.photoUniqueId)
            .param("caption", draft.caption)
            .param("latitude", draft.latitude)
            .param("longitude", draft.longitude)
            .param("updatedAt", Timestamp.from(draft.updatedAt))
            .update()

        return draft
    }

    fun insertSubmission(submission: TelegramSubmission) {
        jdbcClient.sql(
            """
            insert into telegram_submissions (
                id, chat_id, telegram_user_id, username, photo_file_id, photo_unique_id,
                caption, latitude, longitude, status, created_at
            ) values (
                :id, :chatId, :telegramUserId, :username, :photoFileId, :photoUniqueId,
                :caption, :latitude, :longitude, :status, :createdAt
            )
            """.trimIndent(),
        )
            .param("id", submission.id)
            .param("chatId", submission.chatId)
            .param("telegramUserId", submission.telegramUserId)
            .param("username", submission.username)
            .param("photoFileId", submission.photoFileId)
            .param("photoUniqueId", submission.photoUniqueId)
            .param("caption", submission.caption)
            .param("latitude", submission.latitude)
            .param("longitude", submission.longitude)
            .param("status", submission.status)
            .param("createdAt", Timestamp.from(submission.createdAt))
            .update()
    }

    fun markSubmissionProcessing(submissionId: String) {
        updateSubmissionStatus(
            submissionId = submissionId,
            status = "PROCESSING",
            photoId = null,
            errorMessage = null,
            processedAt = null,
        )
    }

    fun markSubmissionPublished(submissionId: String, photoId: String) {
        updateSubmissionStatus(
            submissionId = submissionId,
            status = "PUBLISHED",
            photoId = photoId,
            errorMessage = null,
            processedAt = Instant.now(),
        )
    }

    fun markSubmissionFailed(submissionId: String, errorMessage: String) {
        updateSubmissionStatus(
            submissionId = submissionId,
            status = "FAILED",
            photoId = null,
            errorMessage = errorMessage.take(1000),
            processedAt = Instant.now(),
        )
    }

    fun deleteDraft(chatId: Long) {
        jdbcClient.sql("delete from telegram_submission_drafts where chat_id = :chatId")
            .param("chatId", chatId)
            .update()
    }

    private companion object {
        val draftRowMapper = RowMapper { rs, _ ->
            TelegramSubmissionDraft(
                chatId = rs.getLong("chat_id"),
                telegramUserId = rs.getLong("telegram_user_id"),
                username = rs.getString("username"),
                photoFileId = rs.getString("photo_file_id"),
                photoUniqueId = rs.getString("photo_unique_id"),
                caption = rs.getString("caption"),
                latitude = rs.getObject("latitude") as Double?,
                longitude = rs.getObject("longitude") as Double?,
                updatedAt = rs.getTimestamp("updated_at").toInstant(),
            )
        }
    }

    private fun updateSubmissionStatus(
        submissionId: String,
        status: String,
        photoId: String?,
        errorMessage: String?,
        processedAt: Instant?,
    ) {
        jdbcClient.sql(
            """
            update telegram_submissions
            set status = :status,
                photo_id = coalesce(:photoId, photo_id),
                error_message = :errorMessage,
                processed_at = :processedAt
            where id = :submissionId
            """.trimIndent(),
        )
            .param("status", status)
            .param("photoId", photoId)
            .param("errorMessage", errorMessage)
            .param("processedAt", processedAt?.let(Timestamp::from))
            .param("submissionId", submissionId)
            .update()
    }
}
