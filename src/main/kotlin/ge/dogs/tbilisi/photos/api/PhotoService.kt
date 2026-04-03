package ge.dogs.tbilisi.photos.api

import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class PhotoService(
    private val photoRepository: PhotoRepository,
) {
    fun getPublishedPhotoMarkers(boundingBox: BoundingBox): List<MapPhotoMarkerDto> =
        photoRepository.findPublishedMarkers(boundingBox)

    fun getPhotoById(id: String): PhotoDetailsDto {
        val photo = photoRepository.findPublishedPhotoById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found")
        return photo
    }
}
