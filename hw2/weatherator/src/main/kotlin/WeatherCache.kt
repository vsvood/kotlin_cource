class WeatherCache {
    private val cache = mutableMapOf<String, WeatherResponse>()

    fun generateKey(latitude: Double, longitude: Double, startDate: String, endDate: String): String {
        return "$latitude,$longitude,$startDate,$endDate"
    }

    fun getFromCache(key: String): WeatherResponse? {
        return cache[key]
    }

    fun saveToCache(key: String, data: WeatherResponse) {
        cache[key] = data
    }
}
