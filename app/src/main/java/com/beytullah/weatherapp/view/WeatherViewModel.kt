package com.beytullah.weatherapp.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beytullah.weatherapp.api.WeatherApi
import com.beytullah.weatherapp.model.CurrentWeatherResponse
import com.beytullah.weatherapp.model.WeatherTodayResponse
import com.beytullah.weatherapp.model.WeeklyWeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val weatherApi: WeatherApi) : ViewModel() {

    private val _currentWeatherData = MutableLiveData<Response<CurrentWeatherResponse>>()
    val currentWeatherData: LiveData<Response<CurrentWeatherResponse>> get() = _currentWeatherData

    private val _weeklyWeatherData = MutableLiveData<Response<WeeklyWeatherResponse>>()
    val weeklyWeatherData: LiveData<Response<WeeklyWeatherResponse>> get() = _weeklyWeatherData

    private val _todaysHourlyData = MutableLiveData<Response<WeatherTodayResponse>>()
    val todaysHourlyData: LiveData<Response<WeatherTodayResponse>> get() = _todaysHourlyData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading

    private val _exceptionMessage : MutableLiveData<String> = MutableLiveData()
    val exceptionMessage : LiveData<String> get() = _exceptionMessage

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                fetchWeeklyWeather(latitude, longitude)
                fetchTodaysHourlyTemperature(latitude, longitude)
                fetchCurrentWeather(latitude, longitude)
            } catch (e: Exception) {
                _exceptionMessage.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun fetchCurrentWeather(latitude: Double, longitude: Double) {
        val current = listOf("temperature_2m", "weather_code")
        try {
            val response = weatherApi.getCurrentWeather(latitude, longitude, current, "auto")
            if (!response.isSuccessful) {
                _exceptionMessage.postValue(response.message())
                return
            }
            _currentWeatherData.postValue(response)
        } catch (e: Exception) {
            _exceptionMessage.postValue("error: "+ e.message)
        }
    }

    private suspend fun fetchWeeklyWeather(latitude: Double, longitude: Double) {
        val list : List<String> = listOf("temperature_2m_min", "temperature_2m_max", "weather_code")
        try {
            val response = weatherApi.getMinMaxWeatherWeekly(latitude, longitude, list)
            if (!response.isSuccessful) {
                _exceptionMessage.postValue(response.message())
                return
            }
            _weeklyWeatherData.postValue(response)
        } catch (e: Exception) {
            _exceptionMessage.postValue("error: "+ e.message)
        }
    }

    private suspend fun fetchTodaysHourlyTemperature(latitude: Double, longitude: Double) {
        val temperature = listOf("temperature_2m", "weather_code")
        try {
            val response = weatherApi.getTodaysHourlyTemperature(latitude, longitude, temperature, hour = 12, timezone = "auto")
            if (!response.isSuccessful) {
                _exceptionMessage.postValue(response.message())
                return
            }
            _todaysHourlyData.postValue(response)
        } catch (e: Exception) {
            _exceptionMessage.postValue("error: "+ e.message)
        }
    }

}
