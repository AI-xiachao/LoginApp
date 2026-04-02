package com.example.loginapp.domain.repository

import com.example.loginapp.domain.model.AuthState
import com.example.loginapp.domain.model.LoginCredentials
import com.example.loginapp.domain.model.LoginResult
import com.example.loginapp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库接口 - Domain 层定义，Data 层实现
 */
interface AuthRepository {

    /**
     * 当前认证状态流（热流，持续监听登录态变化）
     */
    val authState: Flow<AuthState>

    /**
     * 登录
     */
    suspend fun login(credentials: LoginCredentials): LoginResult

    /**
     * 登出
     */
    suspend fun logout(): Result<Unit>

    /**
     * 检查并恢复之前的登录状态（App 启动时调用）
     */
    suspend fun restoreSession(): Result<User?>

    /**
     * 获取当前缓存的用户信息
     */
    fun getCurrentUser(): User?
}
