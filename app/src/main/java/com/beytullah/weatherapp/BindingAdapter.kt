package com.beytullah.weatherapp

import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("formattedTemperature")
fun bindFormattedTemperature(textView: TextView, temperature: Double?) {
    temperature?.let {
        textView.text = "Temperature: ${it}°C"
    }
}
