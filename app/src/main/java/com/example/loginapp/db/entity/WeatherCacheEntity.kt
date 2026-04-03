package com.example.loginapp.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val id: Int = 1,
    val temperature: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val uvIndex: Double,
    val precipitationProbability: Int,
    val city: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double
)
