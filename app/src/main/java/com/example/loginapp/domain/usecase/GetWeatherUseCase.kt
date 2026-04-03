package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.Weather
import com.example.loginapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取天气用例 - 返回缓存数据流
 */
class GetWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {

    operator fun invoke(): Flow<Weather?> {
        return weatherRepository.getWeatherStream()
    }
}
