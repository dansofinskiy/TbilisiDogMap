package ge.dogs.tbilisi.photos.api

import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

data class BoundingBox(
    val minLongitude: Double,
    val minLatitude: Double,
    val maxLongitude: Double,
    val maxLatitude: Double,
) {
    companion object {
        fun parse(raw: String): BoundingBox {
            val parts = raw.split(",").map { it.trim() }

            if (parts.size != 4) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "bbox must contain minLng,minLat,maxLng,maxLat",
                )
            }

            val values = parts.map {
                it.toDoubleOrNull() ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "bbox contains an invalid coordinate",
                )
            }

            val minLongitude = values[0]
            val minLatitude = values[1]
            val maxLongitude = values[2]
            val maxLatitude = values[3]

            if (minLongitude > maxLongitude || minLatitude > maxLatitude) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "bbox min values must be less than max values",
                )
            }

            return BoundingBox(
                minLongitude = minLongitude,
                minLatitude = minLatitude,
                maxLongitude = maxLongitude,
                maxLatitude = maxLatitude,
            )
        }
    }
}
