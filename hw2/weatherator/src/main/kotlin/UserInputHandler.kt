import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class UserInputHandler {

    fun readCoordinates(): Pair<Double, Double> {
        while (true) {
            try {
                print("Введите широту и долготу через пробел (например, 55.75 37.61): ")
                val input = readln().trim().split("\\s+".toRegex())

                if (input.size != 2) {
                    println("Ошибка: требуется два числа, разделенных пробелом.")
                    continue
                }

                val latitude = input[0].toDouble().also {
                    require(it in -90.0..90.0) { "Широта должна быть от -90 до 90" }
                }
                val longitude = input[1].toDouble().also {
                    require(it in -180.0..180.0) { "Долгота должна быть от -180 до 180" }
                }

                return Pair(latitude, longitude)
            } catch (e: NumberFormatException) {
                println("Ошибка: неверный формат числа. Введите числа в формате 00.00")
            } catch (e: IllegalArgumentException) {
                println(e.message)
            }
        }
    }

    fun readDateRange(): Pair<String, String> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        while (true) {
            try {
                print("Введите начальную дату в формате ГГГГ-ММ-ДД: ")
                val startDate = LocalDate.parse(readln(), formatter)

                print("Введите конечную дату в формате ГГГГ-ММ-ДД: ")
                val endDate = LocalDate.parse(readln(), formatter)

                require(!endDate.isBefore(startDate)) {
                    "Конечная дата должна быть после начальной"
                }

                return Pair(
                    startDate.format(DateTimeFormatter.ISO_DATE),
                    endDate.format(DateTimeFormatter.ISO_DATE)
                )
            } catch (e: Exception) {
                println("Ошибка: ${e.message ?: "Неверный формат даты. Используйте ГГГГ-ММ-ДД."}")
                println("Пример правильного формата: 2023-12-31")
            }
        }
    }
}
