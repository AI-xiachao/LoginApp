package com.example.loginapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 登录请求 DTO
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String
)
