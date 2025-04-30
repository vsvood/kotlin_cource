data class TemperatureExtremes(
    val minTemp: Double,
    val minTime: String,
    val maxTemp: Double,
    val maxTime: String
)

fun analyzeTemperature(hourlyData: HourlyData): TemperatureExtremes {
    if (hourlyData.time.isEmpty() || hourlyData.temperature2m.isEmpty()) {
        throw IllegalArgumentException("Нет данных для анализа")
    }

    val validTemperatures = hourlyData.temperature2m.filterNotNull()
    if (validTemperatures.isEmpty()) {
        throw IllegalArgumentException("Все значения температуры равны null")
    }

    var minTemp = validTemperatures[0]
    var minTime = hourlyData.time[hourlyData.temperature2m.indexOfFirst { it == minTemp }]
    var maxTemp = validTemperatures[0]
    var maxTime = hourlyData.time[hourlyData.temperature2m.indexOfFirst { it == maxTemp }]

    hourlyData.time.zip(hourlyData.temperature2m).forEach { (time, temp) ->
        temp?.let {
            if (it < minTemp) {
                minTemp = it
                minTime = time
            }
            if (it > maxTemp) {
                maxTemp = it
                maxTime = time
            }
        }
    }

    return TemperatureExtremes(minTemp, minTime, maxTemp, maxTime)
}
