package com.beytullah.weatherapp

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class HourlyWeatherAdapter(
    private val hours: List<String>,
    private val temps: List<Double>,
    private val weatherCodes: List<Int>
) : RecyclerView.Adapter<HourlyWeatherAdapter.WeatherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hourly_weather_list_item, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH", Locale.getDefault())
        val weatherCode = weatherCodes[position]
        val date = inputFormat.parse(hours[position])
        val temp = temps[position]
        val hour = outputFormat.format(date!!)
        val weatherIcon = getWeatherIcons(weatherCode)
        holder.weatherCodeImage.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, weatherIcon))
        holder.hourTextView.text = hour
        holder.tempTextView.text = "${temp.toInt()}°C"
    }

    override fun getItemCount(): Int {
        return hours.size
    }

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hourTextView: TextView = itemView.findViewById(R.id.txtHour)
        val tempTextView: TextView = itemView.findViewById(R.id.txtTemp)
        val weatherCodeImage: ImageView = itemView.findViewById(R.id.imgWeatherCode)
    }

    private fun getWeatherIcons(weatherCode: Int): Int{
        val weatherIcon : Int = when(weatherCode) {
            0,1 -> R.drawable.sunny
            2 -> R.drawable.partly_cloudy
            3 -> R.drawable.cloudy
            else -> R.drawable.rainy
        }
        return weatherIcon
    }
}