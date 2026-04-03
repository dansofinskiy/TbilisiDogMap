package ge.dogs.tbilisi.telegram.api

import org.springframework.stereotype.Service

enum class BotLanguage {
    RU,
    EN,
    KA,
}

@Service
class TelegramBotMessages {
    fun resolve(rawLanguageCode: String?): BotLanguage {
        val normalized = rawLanguageCode?.lowercase().orEmpty()
        return when {
            normalized.startsWith("ka") -> BotLanguage.KA
            normalized.startsWith("ru") -> BotLanguage.RU
            else -> BotLanguage.EN
        }
    }

    fun start(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Пришли фотографию собаки и геолокацию. Их можно отправить в любом порядке."
        BotLanguage.EN -> "Send a dog photo and a location. You can send them in any order."
        BotLanguage.KA -> "გამომიგზავნე ძაღლის ფოტო და გეოლოკაცია. მათი გამოგზავნა შეგიძლია ნებისმიერ რიგში."
    }

    fun sendPhotoOrLocation(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Пожалуйста, пришли фотографию или геолокацию, чтобы я смог создать запись."
        BotLanguage.EN -> "Please send a photo or a geolocation so I can create a submission."
        BotLanguage.KA -> "გთხოვ, გამომიგზავნე ფოტო ან გეოლოკაცია, რომ ჩანაწერი შევქმნა."
    }

    fun requestPhoto(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Геолокация получена. Теперь пришли фотографию собаки."
        BotLanguage.EN -> "Location received. Now send the dog photo."
        BotLanguage.KA -> "გეოლოკაცია მიღებულია. ახლა გამომიგზავნე ძაღლის ფოტო."
    }

    fun requestLocation(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Фотография получена. Теперь пришли геолокацию этой собаки."
        BotLanguage.EN -> "Photo received. Now send the geolocation for this dog."
        BotLanguage.KA -> "ფოტო მიღებულია. ახლა გამომიგზავნე ამ ძაღლის გეოლოკაცია."
    }

    fun published(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Спасибо. Фото прошло проверку на собаку и опубликовано на карте."
        BotLanguage.EN -> "Thanks. The photo passed the dog check and was published on the map."
        BotLanguage.KA -> "გმადლობ. ფოტო ძაღლის შემოწმებას გაიარა და რუკაზე გამოქვეყნდა."
    }

    fun analysisFailed(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Сейчас не удалось проанализировать фотографию. Попробуй еще раз чуть позже."
        BotLanguage.EN -> "I couldn't analyze the photo right now. Please try again a bit later."
        BotLanguage.KA -> "ახლა ფოტოს ანალიზი ვერ მოხერხდა. სცადე ცოტა მოგვიანებით."
    }

    fun rejectedNotDog(language: BotLanguage): String = when (language) {
        BotLanguage.RU -> "Я не смог подтвердить, что на фото собака. Пожалуйста, пришли другую фотографию собаки."
        BotLanguage.EN -> "I could not confirm that the photo contains a dog. Please send another dog photo."
        BotLanguage.KA -> "ვერ დავადასტურე, რომ ფოტოზე ძაღლია. გთხოვ, გამომიგზავნე ძაღლის სხვა ფოტო."
    }
}
