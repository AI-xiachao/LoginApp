package com.example.loginapp.presentation.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.loginapp.domain.model.LoginError
import com.example.loginapp.domain.model.LoginResult
import com.example.loginapp.domain.usecase.LoginUseCase
import com.example.loginapp.presentation.base.BaseViewModel
import com.example.loginapp.presentation.base.UiEffect
import com.example.loginapp.presentation.base.UiEvent
import com.example.loginapp.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LoginViewModel"

/**
 * 登录 ViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginState, LoginEvent, LoginEffect>() {

    init {
        Log.d(TAG, "ViewModel初始化完成")
    }

    override fun initialState(): LoginState = LoginState()

    override fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> {
                Log.v(TAG, "用户名变化 - 新值长度: ${event.value.length}")
                updateState { copy(username = event.value, error = null) }
            }
            is LoginEvent.PasswordChanged -> {
                Log.v(TAG, "密码变化 - 新值长度: ${event.value.length}")
                updateState { copy(password = event.value, error = null) }
            }
            is LoginEvent.LoginClicked -> {
                Log.i(TAG, "用户点击登录按钮 - 用户名: ${currentState.username}")
                login()
            }
            is LoginEvent.DismissError -> {
                Log.d(TAG, "用户关闭错误提示")
                updateState { copy(error = null) }
            }
        }
    }

    private fun login() {
        if (currentState.isLoading) {
            Log.w(TAG, "登录已在进行中，忽略重复请求")
            return
        }

        val username = currentState.username
        Log.i(TAG, "开始登录流程 - 用户名: $username")
        updateState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = loginUseCase(
                    username = username,
                    password = currentState.password
                )

                when (result) {
                    is LoginResult.Success -> {
                        Log.i(TAG, "登录成功 - 用户: ${result.user.username}, 准备导航到首页")
                        updateState { copy(isLoading = false) }
                        sendEffect(LoginEffect.NavigateToHome)
                    }
                    is LoginResult.Failure -> {
                        val errorMessage = when (result.error) {
                            LoginError.InvalidCredentials -> {
                                Log.w(TAG, "登录失败 - 无效凭证")
                                "用户名或密码错误"
                            }
                            LoginError.NetworkError -> {
                                Log.w(TAG, "登录失败 - 网络错误")
                                "网络连接失败，请检查网络"
                            }
                            LoginError.ServerError -> {
                                Log.e(TAG, "登录失败 - 服务器错误")
                                "服务器错误，请稍后再试"
                            }
                            is LoginError.Unknown -> {
                                Log.e(TAG, "登录失败 - 未知错误: ${result.error.message}")
                                result.error.message ?: "未知错误"
                            }
                        }
                        updateState { copy(isLoading = false, error = errorMessage) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "登录流程异常", e)
                updateState { copy(isLoading = false, error = "登录异常: ${e.message}") }
            }
        }
    }
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class LoginEvent : UiEvent {
    data class UsernameChanged(val value: String) : LoginEvent()
    data class PasswordChanged(val value: String) : LoginEvent()
    data object LoginClicked : LoginEvent()
    data object DismissError : LoginEvent()
}

sealed class LoginEffect : UiEffect {
    data object NavigateToHome : LoginEffect()
    data class ShowToast(val message: String) : LoginEffect()
}