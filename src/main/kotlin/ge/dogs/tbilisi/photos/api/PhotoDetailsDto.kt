package ge.dogs.tbilisi.photos.api

data class PhotoDetailsDto(
    val id: String,
    val title: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val aiDescription: String,
    val caption: String,
    val aiConfidence: Double,
    val source: String,
    val imageUrl: String,
    val status: String,
)
