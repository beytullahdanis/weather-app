package com.beytullah.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class WeeklyWeatherAdapter(
    private val dates: List<String>,
    private val minTemps: List<Double>,
    private val maxTemps: List<Double>,
    private val weatherCodes: List<Int>
) : RecyclerView.Adapter<WeeklyWeatherAdapter.WeatherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_list_item, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/M", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

        val weatherCode = weatherCodes[position]
        val weatherIcon = getWeatherIcons(weatherCode)
        val date = inputFormat.parse(dates[position])
        val formattedDate = outputFormat.format(date!!)
        val formattedDay = dayFormat.format(date)
        val minTemp = minTemps[position]
        val maxTemps = maxTemps[position]
        holder.minTempTextView.text = "${minTemp.toInt()}"
        holder.maxTempTextView.text = "${maxTemps.toInt()}°C"
        holder.timeTextView.text = formattedDate
        holder.dayTextView.text = formattedDay
        holder.weatherCodeImage.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, weatherIcon))
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val minTempTextView: TextView = itemView.findViewById(R.id.minTemp)
        val maxTempTextView: TextView = itemView.findViewById(R.id.maxTemp)
        val timeTextView: TextView = itemView.findViewById(R.id.date)
        val dayTextView: TextView = itemView.findViewById(R.id.day)
        val weatherCodeImage: ImageView = itemView.findViewById(R.id.imgWeatherCode)
    }

    fun getWeatherIcons(weatherCode: Int): Int{
        val weatherIcon : Int = when(weatherCode) {
            0,1 -> R.drawable.sunny
            2 -> R.drawable.partly_cloudy
            3 -> R.drawable.cloudy
            else -> R.drawable.rainy
        }
        return weatherIcon
    }
}