package com.example.loginapp.presentation.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.loginapp.domain.model.AuthState
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.usecase.CheckAuthStateUseCase
import com.example.loginapp.domain.usecase.LogoutUseCase
import com.example.loginapp.presentation.base.BaseViewModel
import com.example.loginapp.presentation.base.UiEffect
import com.example.loginapp.presentation.base.UiEvent
import com.example.loginapp.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

/**
 * 首页 ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkAuthStateUseCase: CheckAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<HomeState, HomeEvent, HomeEffect>() {

    override fun initialState(): HomeState = HomeState()

    init {
        Log.d(TAG, "ViewModel初始化 - 开始监听认证状态")
        viewModelScope.launch {
            // 监听认证状态变化
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

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LogoutClicked -> {
                Log.i(TAG, "用户点击登出按钮")
                logout()
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
                        // 状态变化会通过 authState Flow 自动触发 Navigation
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
    val isLoading: Boolean = false
) : UiState

sealed class HomeEvent : UiEvent {
    data object LogoutClicked : HomeEvent()
}

sealed class HomeEffect : UiEffect {
    data object NavigateToLogin : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
}
