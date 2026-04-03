package com.example.loginapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Open-Meteo API 响应对象
 */
data class WeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("daily") val daily: DailyWeather?
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("relative_humidity_2m") val humidity: Int
)

data class DailyWeather(
    @SerializedName("uv_index_max") val uvIndexMax: List<Double?>,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>,
    @SerializedName("time") val time: List<String>
)

/**
 * IP 定位响应对象 (ipwho.is)
 */
data class IpLocationResponse(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("city") val city: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("ip") val ip: String?,
    @SerializedName("success") val success: Boolean = true
)
