package com.example.loginapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 密码登录响应
 *
 * 对应接口: /oauth/new/access_token_password
 */
data class PasswordLoginResponse(
    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("value")
    val value: LoginValue?
)

/**
 * 登录响应值对象
 */
data class LoginValue(
    @SerializedName("oauth_token")
    val oauthToken: String?,

    @SerializedName("oauth_token_secret")
    val oauthTokenSecret: String?,

    @SerializedName("user_id")
    val userId: String?,

    @SerializedName("user_name")
    val userName: String?,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("areaCode")
    val areaCode: String?,

    @SerializedName("icon")
    val icon: String?,

    @SerializedName("isWeak")
    val isWeak: String?,

    @SerializedName("new_user")
    val newUser: String?,

    @SerializedName("flyme")
    val flyme: String?,

    @SerializedName("flymeServiceOut")
    val flymeServiceOut: String?,

    @SerializedName("user_rememberme")
    val userRememberMe: String?
)
