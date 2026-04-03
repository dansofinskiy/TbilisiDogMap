package ge.dogs.tbilisi.photos.api

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

@Repository
class PhotoRepository(
    private val jdbcClient: JdbcClient,
) {
    fun findPublishedMarkers(boundingBox: BoundingBox): List<MapPhotoMarkerDto> = jdbcClient.sql(
        """
        select id, latitude, longitude
        from photos
        where status = 'PUBLISHED'
          and ST_Intersects(
                ST_SetSRID(ST_MakePoint(longitude, latitude), 4326),
                ST_MakeEnvelope(:minLongitude, :minLatitude, :maxLongitude, :maxLatitude, 4326)
          )
        order by created_at desc
        """.trimIndent(),
    )
        .param("minLongitude", boundingBox.minLongitude)
        .param("maxLongitude", boundingBox.maxLongitude)
        .param("minLatitude", boundingBox.minLatitude)
        .param("maxLatitude", boundingBox.maxLatitude)
        .query(markerRowMapper)
        .list()

    fun findPublishedPhotoById(id: String): PhotoDetailsDto? = jdbcClient.sql(
        """
        select id, title, district, latitude, longitude, created_at,
               ai_description, caption, ai_confidence, source, image_url, status
        from photos
        where id = :id and status = 'PUBLISHED'
        """.trimIndent(),
    )
        .param("id", id)
        .query(detailsRowMapper)
        .optional()
        .orElse(null)

    fun insertTelegramPhoto(
        id: String,
        title: String,
        district: String,
        latitude: Double,
        longitude: Double,
        createdAt: Instant,
        aiDescription: String,
        caption: String,
        aiConfidence: Double,
        source: String,
        imageUrl: String,
        status: String,
        sourceSubmissionId: String,
        telegramPhotoFileId: String,
        telegramPhotoUniqueId: String?,
    ) {
        jdbcClient.sql(
            """
            insert into photos (
                id, title, district, latitude, longitude, created_at,
                ai_description, caption, ai_confidence, source, image_url, status,
                source_submission_id, telegram_photo_file_id, telegram_photo_unique_id
            ) values (
                :id, :title, :district, :latitude, :longitude, :createdAt,
                :aiDescription, :caption, :aiConfidence, :source, :imageUrl, :status,
                :sourceSubmissionId, :telegramPhotoFileId, :telegramPhotoUniqueId
            )
            on conflict (id) do nothing
            """.trimIndent(),
        )
            .param("id", id)
            .param("title", title)
            .param("district", district)
            .param("latitude", latitude)
            .param("longitude", longitude)
            .param("createdAt", Timestamp.from(createdAt))
            .param("aiDescription", aiDescription)
            .param("caption", caption)
            .param("aiConfidence", aiConfidence)
            .param("source", source)
            .param("imageUrl", imageUrl)
            .param("status", status)
            .param("sourceSubmissionId", sourceSubmissionId)
            .param("telegramPhotoFileId", telegramPhotoFileId)
            .param("telegramPhotoUniqueId", telegramPhotoUniqueId)
            .update()
    }

    private companion object {
        val markerRowMapper = RowMapper<MapPhotoMarkerDto> { rs, _ ->
            MapPhotoMarkerDto(
                id = rs.getString("id"),
                latitude = rs.getDouble("latitude"),
                longitude = rs.getDouble("longitude"),
            )
        }

        val detailsRowMapper = RowMapper<PhotoDetailsDto> { rs, _ ->
            PhotoDetailsDto(
                id = rs.getString("id"),
                title = rs.getString("title"),
                district = rs.getString("district"),
                latitude = rs.getDouble("latitude"),
                longitude = rs.getDouble("longitude"),
                createdAt = rs.getTimestamp("created_at").toInstant().toString(),
                aiDescription = rs.getString("ai_description"),
                caption = rs.getString("caption"),
                aiConfidence = rs.getDouble("ai_confidence"),
                source = rs.getString("source"),
                imageUrl = rs.getString("image_url"),
                status = rs.getString("status"),
            )
        }
    }
}
