package com.beytullah.weatherapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.beytullah.weatherapp.HourlyWeatherAdapter
import com.beytullah.weatherapp.R
import com.beytullah.weatherapp.WeeklyWeatherAdapter
import com.beytullah.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var adapter: WeeklyWeatherAdapter
    private lateinit var weatherAdapter: HourlyWeatherAdapter
    private var timeList: ArrayList<String> = ArrayList()
    private var minTemperatureList: ArrayList<Double> = ArrayList()
    private var maxTemperatureList: ArrayList<Double> = ArrayList()
    private var weeklyWeatherCodeList: ArrayList<Int> = ArrayList()
    private var hourList: ArrayList<String> = ArrayList()
    private var hourlyTemperatureList: ArrayList<Double> = ArrayList()
    private var weatherCodeList: ArrayList<Int> = ArrayList()
    private var latitude = 0.0
    private var longitude = 0.0
    private var region = ""

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private val permissionId = 2

    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    private lateinit var swipeRefresh : SwipeRefreshLayout
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        toolbar = binding.toolbar
        swipeRefresh = binding.swipeRefresh
        val refreshButton : Button = binding.refreshWeatherData
        toolbar.setTitle("Weather App")
        val recyclerView = binding.weatherList
        val weatherRecyclerView = binding.hourlyWeatherList
        recyclerView.layoutManager = LinearLayoutManager(this)
        weatherRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = WeeklyWeatherAdapter(timeList, minTemperatureList, maxTemperatureList, weeklyWeatherCodeList)
        weatherAdapter = HourlyWeatherAdapter(hourList, hourlyTemperatureList, weatherCodeList)
        recyclerView.adapter = adapter
        weatherRecyclerView.adapter = weatherAdapter

        startProcess()

        viewModel.weeklyWeatherData.observe(this@MainActivity, Observer { weatherDataResult ->
            val lastindex = weatherDataResult.body()?.daily?.time?.size?.minus(1)
            for (i in 1..lastindex!!) {
                timeList.add(weatherDataResult.body()!!.daily.time[i])
                minTemperatureList.add(weatherDataResult.body()!!.daily.temperature_2m_min[i])
                maxTemperatureList.add(weatherDataResult.body()!!.daily.temperature_2m_max[i])
                weeklyWeatherCodeList.add(weatherDataResult?.body()!!.daily.weather_code[i])
            }
            adapter.notifyDataSetChanged()

        })

        viewModel.todaysHourlyData.observe(this, Observer { result ->
            val lastindex = result.body()?.hourly?.time?.size?.minus(1)
            for (i in 0..lastindex!!) {
                hourList.add(result.body()!!.hourly.time[i])
                hourlyTemperatureList.add(result.body()!!.hourly.temperature_2m[i])
                weatherCodeList.add(result?.body()!!.hourly.weather_code[i])
            }
            weatherAdapter.notifyDataSetChanged()
        })

        viewModel.currentWeatherData.observe(this, Observer {
            val temperature = it.body()?.current?.temperature_2m?.toInt().toString() + "°"
            val weatherCode = it.body()?.current?.weather_code!!
            val region = it.body()?.timezone.toString()

            binding.txtCurrent.text = temperature
            binding.txtCurrent.setTextColor(getWeatherTextColor(weatherCode))
            binding.txtRegion.text = region
        })

        viewModel.exceptionMessage.observe(this@MainActivity, Observer {
            Toast.makeText(this, "Hata", Toast.LENGTH_SHORT).show()
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                binding.refreshWeatherData.isEnabled = false
                binding.pBar.visibility = View.VISIBLE
                binding.weatherList.visibility = View.INVISIBLE
                binding.hourlyWeatherList.visibility = View.INVISIBLE
                binding.txtCurrent.visibility = View.INVISIBLE
                binding.txtWeather.visibility = View.INVISIBLE
                binding.txtRegion.visibility = View.INVISIBLE
                binding.cardView.visibility = View.INVISIBLE

            } else {
                binding.pBar.visibility = View.GONE
                binding.refreshWeatherData.visibility = View.VISIBLE
                binding.refreshWeatherData.isEnabled = true
                binding.weatherList.visibility = View.VISIBLE
                binding.hourlyWeatherList.visibility = View.VISIBLE
                binding.txtCurrent.visibility = View.VISIBLE
                binding.txtWeather.visibility = View.VISIBLE
                binding.txtRegion.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
            }
        })

        refreshButton.setOnClickListener {
            binding.refreshWeatherData.visibility = View.GONE
            startProcess()
        }

        swipeRefresh.setOnRefreshListener {
            binding.refreshWeatherData.visibility = View.GONE
            startProcess()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        latitude = list?.get(0)?.latitude!!
                        longitude = list?.get(0)?.longitude!!
                        region = list?.get(0)?.adminArea!!
                        viewModel.fetchWeatherData(latitude, longitude)
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    fun getWeatherTextColor(weatherCode: Int): Int{
         val textColor: Int = when(weatherCode) {
            0,1 ->  ContextCompat.getColor(binding.txtWeather.context, R.color.orange)
            2 ->  ContextCompat.getColor(binding.txtWeather.context, R.color.light_orange)
            3 ->  ContextCompat.getColor(binding.txtWeather.context, R.color.light_white)
            else ->  ContextCompat.getColor(binding.txtWeather.context, R.color.light_blue)
        }
        return textColor
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun startProcess(){
        timeList.clear()
        minTemperatureList.clear()
        maxTemperatureList.clear()
        weatherCodeList.clear()
        hourList.clear()
        hourlyTemperatureList.clear()
        weeklyWeatherCodeList.clear()
        adapter.notifyDataSetChanged()
        weatherAdapter.notifyDataSetChanged()
        getLocation()
    }
}