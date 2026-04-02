package com.example.loginapp.data.datasource

import com.example.loginapp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 账户数据源接口
 *
 * 封装底层账户存储（AccountManager/Keychain/Keystore 等），
 * 为 Repository 提供统一的账户数据访问接口。
 * 使用 Domain 层的 User 模型，避免数据模型重复定义。
 */
interface AccountDataSource {

    /**
     * 保存账户信息
     *
     * @param user 用户信息（Domain 层模型）
     * @return 保存成功返回 Result.success，失败返回 Result.failure
     */
    suspend fun saveAccount(user: User): Result<Unit>

    /**
     * 获取已保存的账户信息
     *
     * @return 用户信息，未登录返回 null
     */
    suspend fun getAccount(): User?

    /**
     * 清除账户信息（登出）
     *
     * @return 清除成功返回 Result.success
     */
    suspend fun clearAccount(): Result<Unit>

    /**
     * 监听登录状态变化
     *
     * @return 登录状态流，true 表示已登录
     */
    fun observeLoginState(): Flow<Boolean>

    /**
     * 检查是否已登录
     *
     * @return 已登录返回 true
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * 获取当前 Token
     *
     * @return 有效 Token，未登录返回 null
     */
    suspend fun getToken(): String?
}
