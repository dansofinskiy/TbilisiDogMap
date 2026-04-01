package ge.dogs.tbilisi.photos.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PhotoController(
    private val photoService: PhotoService,
) {
    @GetMapping("/api/map/photos")
    fun getMapPhotos(): List<MapPhotoMarkerDto> = photoService.getPublishedPhotoMarkers()

    @GetMapping("/api/photos/{id}")
    fun getPhotoById(@PathVariable id: String): PhotoDetailsDto = photoService.getPhotoById(id)
}
