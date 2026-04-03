package com.example.loginapp.presentation.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.loginapp.domain.model.AuthState
import com.example.loginapp.domain.model.RandomUser
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.model.Weather
import com.example.loginapp.domain.usecase.CheckAuthStateUseCase
import com.example.loginapp.domain.usecase.FetchRandomUserUseCase
import com.example.loginapp.domain.usecase.FetchWeatherUseCase
import com.example.loginapp.domain.usecase.GetRandomUserUseCase
import com.example.loginapp.domain.usecase.GetWeatherUseCase
import com.example.loginapp.domain.usecase.LogoutUseCase
import com.example.loginapp.domain.repository.WeatherRepository
import com.example.loginapp.presentation.base.BaseViewModel
import com.example.loginapp.presentation.base.UiEffect
import com.example.loginapp.presentation.base.UiEvent
import com.example.loginapp.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkAuthStateUseCase: CheckAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getRandomUserUseCase: GetRandomUserUseCase,
    private val fetchRandomUserUseCase: FetchRandomUserUseCase,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val fetchWeatherUseCase: FetchWeatherUseCase,
    private val weatherRepository: WeatherRepository
) : BaseViewModel<HomeState, HomeEvent, HomeEffect>() {

    override fun initialState(): HomeState = HomeState()

    init {
        Log.d(TAG, "ViewModel初始化 - 开始监听认证状态和缓存")
        viewModelScope.launch {
            getRandomUserUseCase().collect { cachedUser ->
                Log.d(TAG, "缓存数据更新 - ${cachedUser?.fullName ?: "null"}")
                updateState { copy(randomUser = cachedUser) }
            }
        }

        viewModelScope.launch {
            getWeatherUseCase().collect { weather ->
                Log.d(TAG, "天气数据更新 - ${weather?.city ?: "null"}")
                updateState { copy(weather = weather) }
            }
        }

        viewModelScope.launch {
            checkAuthStateUseCase().collect { authState ->
                Log.d(TAG, "认证状态变化 - 新状态: ${authState::class.simpleName}")

                when (authState) {
                    is AuthState.Authenticated -> {
                        Log.i(TAG, "用户已认证 - 用户名: ${authState.user.username}")
                        updateState {
                            copy(
                                user = authState.user,
                                isLoading = false
                            )
                        }
                        loadInitialData()
                    }
                    is AuthState.Unauthenticated -> {
                        Log.w(TAG, "用户未认证 - 导航到登录页")
                        sendEffect(HomeEffect.NavigateToLogin)
                    }
                    is AuthState.Error -> {
                        Log.e(TAG, "认证错误 - 消息: ${authState.message}")
                        updateState { copy(isLoading = false) }
                        sendEffect(HomeEffect.ShowError(authState.message))
                    }
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 检查随机用户缓存
            val cachedUser = getRandomUserUseCase().first()
            if (cachedUser == null) {
                Log.d(TAG, "随机用户缓存为空，自动请求网络数据")
                fetchFromNetwork()
            } else {
                Log.d(TAG, "使用随机用户缓存数据: ${cachedUser.fullName}")
            }

            // 检查天气缓存
            if (!weatherRepository.isCacheValid()) {
                Log.d(TAG, "天气缓存无效或过期，自动请求网络数据")
                fetchWeather()
            } else {
                Log.d(TAG, "天气缓存有效，使用缓存数据")
            }
        }
    }

    private suspend fun fetchFromNetwork() {
        updateState { copy(isRefreshing = true, errorMessage = null) }
        Log.d(TAG, "开始从网络获取...")

        fetchRandomUserUseCase().fold(
            onSuccess = { user ->
                Log.i(TAG, "网络请求成功 - 姓名: ${user.fullName}")
                updateState {
                    copy(
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            },
            onFailure = { error ->
                Log.e(TAG, "网络请求失败", error)
                updateState {
                    copy(
                        isRefreshing = false,
                        errorMessage = error.message ?: "加载失败"
                    )
                }
                sendEffect(HomeEffect.ShowError("刷新失败: ${error.message}"))
            }
        )
    }

    private suspend fun fetchWeather() {
        updateState { copy(isWeatherLoading = true, weatherError = null) }
        Log.d(TAG, "开始获取天气数据...")

        fetchWeatherUseCase().fold(
            onSuccess = { weather ->
                Log.i(TAG, "天气获取成功 - ${weather.city}: ${weather.temperature}°C")
                updateState {
                    copy(
                        isWeatherLoading = false,
                        weatherError = null
                    )
                }
            },
            onFailure = { error ->
                Log.e(TAG, "天气获取失败", error)
                updateState {
                    copy(
                        isWeatherLoading = false,
                        weatherError = error.message ?: "获取天气失败"
                    )
                }
                sendEffect(HomeEffect.ShowError("天气获取失败: ${error.message}"))
            }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            Log.i(TAG, "用户触发刷新")
            updateState { copy(isRefreshing = true, isWeatherLoading = true, errorMessage = null, weatherError = null) }

            // 刷新随机用户
            val userResult = fetchRandomUserUseCase()
            userResult.fold(
                onSuccess = { user ->
                    Log.i(TAG, "随机用户刷新成功 - 姓名: ${user.fullName}")
                },
                onFailure = { error ->
                    Log.e(TAG, "随机用户刷新失败", error)
                }
            )

            // 刷新天气
            val weatherResult = fetchWeatherUseCase()
            weatherResult.fold(
                onSuccess = { weather ->
                    Log.i(TAG, "天气刷新成功 - ${weather.city}: ${weather.temperature}°C")
                },
                onFailure = { error ->
                    Log.e(TAG, "天气刷新失败", error)
                }
            )

            // 更新 UI 状态
            updateState {
                copy(
                    isRefreshing = false,
                    isWeatherLoading = false,
                    errorMessage = userResult.exceptionOrNull()?.message,
                    weatherError = weatherResult.exceptionOrNull()?.message
                )
            }

            if (userResult.isSuccess && weatherResult.isSuccess) {
                sendEffect(HomeEffect.ShowRefreshSuccess)
            } else if (userResult.isFailure || weatherResult.isFailure) {
                val errorMsg = userResult.exceptionOrNull()?.message ?: weatherResult.exceptionOrNull()?.message
                sendEffect(HomeEffect.ShowError("刷新失败: ${errorMsg ?: "未知错误"}"))
            }
        }
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LogoutClicked -> {
                Log.i(TAG, "用户点击登出按钮")
                logout()
            }
            is HomeEvent.RetryRandomUser -> {
                Log.i(TAG, "用户点击重试")
                refresh()
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "开始登出流程...")
            updateState { copy(isLoading = true) }

            try {
                val result = logoutUseCase()

                result.fold(
                    onSuccess = {
                        Log.i(TAG, "登出成功 - 等待状态流触发导航")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "登出失败 - 错误: ${error.message}", error)
                        updateState { copy(isLoading = false) }
                        sendEffect(HomeEffect.ShowError("登出失败: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "登出异常", e)
                updateState { copy(isLoading = false) }
                sendEffect(HomeEffect.ShowError("登出异常: ${e.message}"))
            }
        }
    }
}

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val randomUser: RandomUser? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val weather: Weather? = null,
    val isWeatherLoading: Boolean = false,
    val weatherError: String? = null
) : UiState

sealed class HomeEvent : UiEvent {
    data object LogoutClicked : HomeEvent()
    data object RetryRandomUser : HomeEvent()
}

sealed class HomeEffect : UiEffect {
    data object NavigateToLogin : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
    data object ShowRefreshSuccess : HomeEffect()
}