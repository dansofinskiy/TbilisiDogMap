package ge.dogs.tbilisi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${app.cors.allowed-origin-patterns:http://localhost:3000,http://127.0.0.1:5500,http://localhost:5500,https://*.github.io}")
    private val allowedOriginPatterns: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns(*allowedOriginPatterns.split(",").map { it.trim() }.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
    }
}
