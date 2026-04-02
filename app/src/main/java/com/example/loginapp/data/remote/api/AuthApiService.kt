package com.example.loginapp.data.remote.api

import com.example.loginapp.data.remote.dto.LoginRequest
import com.example.loginapp.data.remote.dto.LoginResponse
import com.example.loginapp.data.remote.dto.PasswordLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * 认证 API 接口
 */
interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/auth/refresh")
    suspend fun refreshToken(): Response<LoginResponse>

    /**
     * Mock 登录方法（当前使用）
     */
    suspend fun loginWithPassword(
        accountBelong: String = "meizu",
        authMode: String = "client_flyme_auth",
        username: String,
        password: String,
        deviceId: String,
        appVersion: String,
        grantType: String? = null,
        signatureParams: Map<String, String> = emptyMap()
    ): Response<PasswordLoginResponse>

    /*
     * 真实密码登录接口（已注释，当前使用 Mock）
     *
     * @param accountBelong 账号归属，固定为 "meizu"
     * @param authMode 授权类型，固定为 "client_flyme_auth"
     * @param username 用户名（已自动补全 @flyme.cn）
     * @param password 密码
     * @param deviceId 设备ID（OAID）
     * @param appVersion 应用版本号
     * @param grantType 授权类型，可选 "password"
     * @param signatureParams 签名参数（占位）
     */
    /*
    @POST("/oauth/new/access_token_password")
    suspend fun loginWithPassword(
        @Query("account_belong") accountBelong: String = "meizu",
        @Query("x_auth_mode") authMode: String = "client_flyme_auth",
        @Query("x_auth_username") username: String,
        @Query("x_auth_password") password: String,
        @Query("device_id") deviceId: String,
        @Query("App-Version") appVersion: String,
        @Query("grant_type") grantType: String? = null,
        @QueryMap signatureParams: Map<String, String> = emptyMap()
    ): Response<PasswordLoginResponse>
    */
}
