package com.beytullah.weatherapp.model

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: HourlyData
)
data class CurrentWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentData,
    val timezone: String
)

data class WeeklyWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val daily: DailyData
)

data class WeatherTodayResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: HourlyData
)

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

data class DailyData(
    val time: List<String>,
    val temperature_2m_min: List<Double>,
    val temperature_2m_max: List<Double>,
    val weather_code: List<Int>

)

data class CurrentData(
    val temperature_2m: Double,
    val weather_code: Int,
)
