package com.example.loginapp.data.repository

import android.util.Log
import com.example.loginapp.data.remote.api.IpLocationService
import com.example.loginapp.data.remote.api.WeatherApiService
import com.example.loginapp.data.remote.dto.WeatherResponse
import com.example.loginapp.db.dao.WeatherCacheDao
import com.example.loginapp.db.entity.WeatherCacheEntity
import com.example.loginapp.domain.model.Weather
import com.example.loginapp.domain.model.getWeatherDescription
import com.example.loginapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WeatherRepository"
private const val CACHE_VALIDITY_HOURS = 1
private const val CACHE_VALIDITY_MILLIS = CACHE_VALIDITY_HOURS * 60 * 60 * 1000L

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val ipLocationService: IpLocationService,
    private val weatherCacheDao: WeatherCacheDao
) : WeatherRepository {

    override fun getWeatherStream(): Flow<Weather?> {
        return weatherCacheDao.getWeatherCache().map { entity ->
            entity?.let {
                Weather(
                    temperature = it.temperature,
                    weatherCode = it.weatherCode,
                    weatherDescription = it.weatherDescription,
                    uvIndex = it.uvIndex,
                    precipitationProbability = it.precipitationProbability,
                    city = it.city,
                    humidity = 0 // 缓存中不存储湿度，从网络获取时才更新
                )
            }
        }
    }

    override suspend fun refreshWeather(): Result<Weather> {
        Log.d(TAG, "开始刷新天气数据...")
        return try {
            // 1. 获取 IP 定位
            val locationResult = getLocationByIp()
            if (locationResult.isFailure) {
                Log.e(TAG, "IP定位失败", locationResult.exceptionOrNull())
                return Result.failure(locationResult.exceptionOrNull() ?: Exception("定位失败"))
            }

            val location = locationResult.getOrNull()!!
            Log.d(TAG, "IP定位成功: ${location.city}, 坐标: ${location.latitude}, ${location.longitude}")

            // 2. 获取天气数据
            val weatherResult = fetchWeatherFromApi(location.latitude, location.longitude)
            if (weatherResult.isFailure) {
                Log.e(TAG, "获取天气失败", weatherResult.exceptionOrNull())
                return Result.failure(weatherResult.exceptionOrNull() ?: Exception("获取天气失败"))
            }

            val weather = weatherResult.getOrNull()!!
            val weatherWithCity = weather.copy(city = location.city)

            // 3. 缓存到数据库
            saveWeatherToCache(weatherWithCity, location.latitude, location.longitude)
            Log.i(TAG, "天气数据刷新成功: ${weatherWithCity.city}, ${weatherWithCity.temperature}°C")

            Result.success(weatherWithCity)
        } catch (e: IOException) {
            Log.e(TAG, "网络异常", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "刷新天气异常", e)
            Result.failure(e)
        }
    }

    override suspend fun isCacheValid(): Boolean {
        val lastCacheTime = weatherCacheDao.getLastCacheTime()
        if (lastCacheTime == null) {
            Log.d(TAG, "缓存无效: 无缓存数据")
            return false
        }

        val elapsed = System.currentTimeMillis() - lastCacheTime
        val isValid = elapsed < CACHE_VALIDITY_MILLIS
        Log.d(TAG, "缓存检查: 已过去 ${elapsed / 1000 / 60} 分钟, 有效: $isValid")
        return isValid
    }

    private suspend fun getLocationByIp(): Result<LocationInfo> {
        return try {
            val response = ipLocationService.getLocationByIp()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.latitude != null && body.longitude != null) {
                    val city = body.city ?: body.region ?: "未知城市"
                    Result.success(LocationInfo(body.latitude, body.longitude, city))
                } else {
                    Result.failure(Exception("定位数据不完整"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchWeatherFromApi(latitude: Double, longitude: Double): Result<Weather> {
        return try {
            val response = weatherApiService.getWeather(latitude, longitude)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(parseWeatherResponse(body))
                } else {
                    Result.failure(Exception("天气数据为空"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseWeatherResponse(response: WeatherResponse): Weather {
        val current = response.current
            ?: throw Exception("当前天气数据为空")

        val daily = response.daily
        val uvIndex = daily?.uvIndexMax?.firstOrNull() ?: 0.0
        val precipitationProb = daily?.precipitationProbabilityMax?.firstOrNull() ?: 0

        return Weather(
            temperature = current.temperature,
            weatherCode = current.weatherCode,
            weatherDescription = getWeatherDescription(current.weatherCode),
            uvIndex = uvIndex,
            precipitationProbability = precipitationProb,
            city = "", // 由调用方填充
            humidity = current.humidity
        )
    }

    private suspend fun saveWeatherToCache(weather: Weather, latitude: Double, longitude: Double) {
        val entity = WeatherCacheEntity(
            id = 1,
            temperature = weather.temperature,
            weatherCode = weather.weatherCode,
            weatherDescription = weather.weatherDescription,
            uvIndex = weather.uvIndex,
            precipitationProbability = weather.precipitationProbability,
            city = weather.city,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )
        weatherCacheDao.saveWeatherCache(entity)
    }

    private data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val city: String
    )
}
