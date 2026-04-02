package com.example.loginapp.domain.model

/**
 * 用户领域模型 - 纯 Kotlin，无 Android 依赖
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val token: String
)
