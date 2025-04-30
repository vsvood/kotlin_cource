import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val userInputHandler = UserInputHandler()
    val apiClient = WeatherApiClient()

    println("Программа анализа исторических температурных данных")

    try {
        val (latitude, longitude) = userInputHandler.readCoordinates()
        val (startDate, endDate) = userInputHandler.readDateRange()

        println("Загрузка данных с API...")
        val weatherData = apiClient.getWeatherData(latitude, longitude, startDate, endDate)

        try {
            val extremes = analyzeTemperature(weatherData.hourly)
            println("Минимальная температура: ${extremes.minTemp}°C, время: ${extremes.minTime}")
            println("Максимальная температура: ${extremes.maxTemp}°C, время: ${extremes.maxTime}")
        } catch (e: IllegalArgumentException) {
            println("Ошибка анализа данных: ${e.message}")
        }

    } catch (e: Exception) {
        println("Ошибка при выполнении программы: ${e.message}")
    }
}
