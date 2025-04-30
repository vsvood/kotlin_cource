import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class WeatherApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                coerceInputValues = true  // This will convert nulls to default values
            })
        }
    }

    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        startDate: String,
        endDate: String
    ): WeatherResponse {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("hourly", "temperature_2m")
            parameter("start_date", startDate)
            parameter("end_date", endDate)
            parameter("timezone", "auto")
        }.body()
    }
}
