package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.Weather
import com.example.loginapp.domain.repository.WeatherRepository
import javax.inject.Inject

/**
 * 获取天气用例 - 从网络获取并缓存
 */
class FetchWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {

    suspend operator fun invoke(): Result<Weather> {
        return weatherRepository.refreshWeather()
    }
}
