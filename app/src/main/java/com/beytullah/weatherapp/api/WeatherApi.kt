package com.beytullah.weatherapp.api

import com.beytullah.weatherapp.model.CurrentWeatherResponse
import com.beytullah.weatherapp.model.WeatherResponse
import com.beytullah.weatherapp.model.WeatherTodayResponse
import com.beytullah.weatherapp.model.WeeklyWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: List<String>,
        @Query("timezone") timezone: String
    ): Response<CurrentWeatherResponse>

    @GET("forecast")
    suspend fun getMinMaxWeatherWeekly(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: List<String>
    ): Response<WeeklyWeatherResponse>

    @GET("forecast")
    suspend fun getTodaysHourlyTemperature(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: List<String>,
        @Query("forecast_hours") hour: Int,
        @Query("timezone") timezone: String
    ): Response<WeatherTodayResponse>

}