package com.example.loginapp.data.remote.api

import com.example.loginapp.data.remote.dto.LoginRequest
import com.example.loginapp.data.remote.dto.LoginResponse
import com.example.loginapp.data.remote.dto.LoginValue
import com.example.loginapp.data.remote.dto.PasswordLoginResponse
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock API 实现 - 用于演示登录功能
 *
 * 测试账号:
 * - 用户名: admin, 密码: 123456
 * - 用户名: user, 密码: password
 */
@Singleton
class MockAuthApiService @Inject constructor() : AuthApiService {

    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        // 模拟网络延迟
        delay(1000)

        // 模拟验证逻辑
        return when {
            request.username == "admin" && request.password == "123456" -> {
                Response.success(
                    LoginResponse(
                        userId = "1",
                        username = "admin",
                        email = "admin@example.com",
                        token = "mock_token_admin_${System.currentTimeMillis()}"
                    )
                )
            }
            request.username == "user" && request.password == "password" -> {
                Response.success(
                    LoginResponse(
                        userId = "2",
                        username = "user",
                        email = "user@example.com",
                        token = "mock_token_user_${System.currentTimeMillis()}"
                    )
                )
            }
            request.username.isBlank() || request.password.isBlank() -> {
                Response.error(400, okhttp3.ResponseBody.create(null, "Empty credentials"))
            }
            else -> {
                // 401 Unauthorized
                Response.error(401, okhttp3.ResponseBody.create(null, "Invalid credentials"))
            }
        }
    }

    override suspend fun logout(): Response<Unit> {
        delay(500)
        return Response.success(Unit)
    }

    override suspend fun refreshToken(): Response<LoginResponse> {
        delay(500)
        return Response.success(
            LoginResponse(
                userId = "1",
                username = "admin",
                email = "admin@example.com",
                token = "refreshed_token_${System.currentTimeMillis()}"
            )
        )
    }

    override suspend fun loginWithPassword(
        accountBelong: String,
        authMode: String,
        username: String,
        password: String,
        deviceId: String,
        appVersion: String,
        grantType: String?,
        signatureParams: Map<String, String>
    ): Response<PasswordLoginResponse> {
        delay(1000)

        // Mock 验证逻辑
        return when {
            username == "admin@flyme.cn" && password == "123456" -> {
                Response.success(
                    PasswordLoginResponse(
                        code = "200",
                        message = "success",
                        value = LoginValue(
                            oauthToken = "mock_token_${System.currentTimeMillis()}",
                            oauthTokenSecret = "mock_secret",
                            userId = "1",
                            userName = "admin",
                            nickname = "管理员",
                            phone = "13800138000",
                            areaCode = null,
                            icon = null,
                            isWeak = null,
                            newUser = null,
                            flyme = null,
                            flymeServiceOut = null,
                            userRememberMe = null
                        )
                    )
                )
            }
            username == "test@flyme.cn" && password == "123456" -> {
                Response.success(
                    PasswordLoginResponse(
                        code = "200",
                        message = "success",
                        value = LoginValue(
                            oauthToken = "mock_token_${System.currentTimeMillis()}",
                            oauthTokenSecret = "mock_secret",
                            userId = "2",
                            userName = "test",
                            nickname = "测试用户",
                            phone = "13800138001",
                            areaCode = null,
                            icon = null,
                            isWeak = null,
                            newUser = null,
                            flyme = null,
                            flymeServiceOut = null,
                            userRememberMe = null
                        )
                    )
                )
            }
            username.isBlank() || password.isBlank() -> {
                Response.success(
                    PasswordLoginResponse(
                        code = "400",
                        message = "用户名或密码不能为空",
                        value = null
                    )
                )
            }
            else -> {
                Response.success(
                    PasswordLoginResponse(
                        code = "401",
                        message = "用户名或密码错误",
                        value = null
                    )
                )
            }
        }
    }
}
