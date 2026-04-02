package com.example.loginapp.domain.model

/**
 * 登录凭证领域模型
 */
data class LoginCredentials(
    val username: String,
    val password: String
)

/**
 * 登录结果封装
 */
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Failure(val error: LoginError) : LoginResult()
}

sealed class LoginError {
    data object InvalidCredentials : LoginError()
    data object NetworkError : LoginError()
    data object ServerError : LoginError()
    data class Unknown(val message: String) : LoginError()
}
