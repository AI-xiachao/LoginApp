package com.example.loginapp.domain.model

/**
 * 认证状态
 */
sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
