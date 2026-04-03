package com.example.loginapp.domain.repository

import com.example.loginapp.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    /**
     * 获取天气数据流（自动处理缓存逻辑）
     * 1小时内直接返回缓存，超过1小时或首次加载则请求网络
     */
    fun getWeatherStream(): Flow<Weather?>

    /**
     * 强制刷新天气数据（忽略缓存）
     */
    suspend fun refreshWeather(): Result<Weather>

    /**
     * 检查缓存是否在有效期内（1小时）
     */
    suspend fun isCacheValid(): Boolean
}
