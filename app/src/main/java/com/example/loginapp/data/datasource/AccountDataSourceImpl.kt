package com.example.loginapp.data.datasource

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.loginapp.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AccountDataSource"

/**
 * AccountDataSource 实现 - 基于 Android AccountManager
 *
 * 数据存储结构：
 * - account.name: 用户名（唯一标识）
 * - account.type: 账户类型（应用包名.account）
 * - userData: userId, email（附加数据）
 * - authToken: Bearer Token（认证令牌）
 *
 * 注意：该类完全封装 Android AccountManager，不向外暴露 Android API
 */
@Singleton
class AccountDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AccountDataSource {

    private val accountManager: AccountManager = AccountManager.get(context)

    override suspend fun saveAccount(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "开始保存账户 - 用户ID: ${user.id}, 用户名: ${user.username}")

        try {
            // 先清除旧账户（保证单点登录）
            val existingAccount = getAccountInternal()
            if (existingAccount != null) {
                Log.d(TAG, "发现已有账户(${existingAccount.name})，先清除旧账户")
                clearAccountInternal()
            }

            val account = Account(user.username, ACCOUNT_TYPE)

            // 1. 添加账户到系统
            val userdata = Bundle().apply {
                putString(KEY_USER_ID, user.id)
                putString(KEY_EMAIL, user.email)
            }

            val added = accountManager.addAccountExplicitly(account, null, userdata)
            if (!added) {
                Log.e(TAG, "添加账户失败 - addAccountExplicitly返回false")
                return@withContext Result.failure(
                    AccountException("添加账户失败，可能已存在相同账户")
                )
            }
            Log.d(TAG, "账户添加成功 - 用户名: ${user.username}")

            // 2. 保存认证 Token
            accountManager.setAuthToken(account, TOKEN_TYPE, user.token)
            Log.d(TAG, "Token保存成功 - Token前缀: ${user.token.take(10)}...")

            Log.i(TAG, "账户保存完成 - 用户: ${user.username}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "保存账户异常 - 用户: ${user.username}, 错误: ${e.message}", e)
            Result.failure(AccountException("保存账户失败: ${e.message}", e))
        }
    }

    override suspend fun getAccount(): User? = withContext(Dispatchers.IO) {
        Log.d(TAG, "开始获取账户信息...")

        val account = getAccountInternal()
        if (account == null) {
            Log.d(TAG, "无保存的账户")
            return@withContext null
        }

        Log.d(TAG, "找到账户 - 用户名: ${account.name}")

        val user = parseToUser(account)
        if (user == null) {
            Log.w(TAG, "账户数据不完整，无法解析为User - 用户名: ${account.name}")
        } else {
            Log.d(TAG, "账户解析成功 - 用户ID: ${user.id}, 用户名: ${user.username}")
        }

        user
    }

    override suspend fun clearAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "开始清除账户...")

        try {
            val existingAccount = getAccountInternal()
            if (existingAccount == null) {
                Log.w(TAG, "清除账户时发现无账户可清除")
                return@withContext Result.success(Unit)
            }

            Log.d(TAG, "清除账户 - 用户名: ${existingAccount.name}")
            clearAccountInternal()
            Log.i(TAG, "账户清除成功")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "清除账户异常 - 错误: ${e.message}", e)
            Result.failure(AccountException("清除账户失败: ${e.message}", e))
        }
    }

    override fun observeLoginState(): Flow<Boolean> = callbackFlow {
        Log.d(TAG, "开始监听登录状态变化...")

        // 使用 runBlocking 确保初始状态与监听器状态一致，避免竞态条件
        val initialState = runBlocking { isLoggedIn() }
        Log.d(TAG, "初始登录状态: $initialState")
        trySend(initialState)

        // 创建监听器监听账户变化
        val listener = OnAccountsUpdateListener { accounts: Array<out Account> ->
            val hasOurAccount = accounts.any { account -> account.type == ACCOUNT_TYPE }
            Log.d(TAG, "账户列表更新 - 包含本应用账户: $hasOurAccount")
            launch {
                trySend(hasOurAccount)
            }
        }

        // 注册监听器
        accountManager.addOnAccountsUpdatedListener(
            listener,
            null,  // handler，null 表示在主线程回调
            false  // 不立即更新
        )
        Log.d(TAG, "已注册账户变化监听器")

        // 清理时移除监听器
        awaitClose {
            Log.d(TAG, "停止监听登录状态变化")
            accountManager.removeOnAccountsUpdatedListener(listener)
        }
    }.distinctUntilChanged()  // 状态变化时才发射

    override suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        val loggedIn = getAccountInternal() != null
        Log.v(TAG, "检查登录状态: $loggedIn")
        loggedIn
    }

    override suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        val token = getAccountInternal()?.let { account ->
            accountManager.peekAuthToken(account, TOKEN_TYPE)
        }

        if (token != null) {
            Log.v(TAG, "获取Token成功 - 前缀: ${token.take(10)}...")
        } else {
            Log.w(TAG, "获取Token失败 - 无有效账户或Token")
        }

        token
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 获取当前应用的账户（内部使用）
     */
    private fun getAccountInternal(): Account? {
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
        Log.v(TAG, "获取账户列表 - 类型: $ACCOUNT_TYPE, 数量: ${accounts.size}")
        return accounts.firstOrNull()
    }

    /**
     * 将 Android Account 解析为 Domain User（内部使用）
     */
    private fun parseToUser(account: Account): User? {
        val userId = accountManager.getUserData(account, KEY_USER_ID)
        val email = accountManager.getUserData(account, KEY_EMAIL) ?: ""
        val token = accountManager.peekAuthToken(account, TOKEN_TYPE)

        if (userId == null) {
            Log.w(TAG, "解析账户失败 - userId为空")
            return null
        }
        if (token == null) {
            Log.w(TAG, "解析账户失败 - token为空")
            return null
        }

        return User(
            id = userId,
            username = account.name,
            email = email,
            token = token
        )
    }

    /**
     * 清除账户（内部使用，不切换线程）
     */
    private fun clearAccountInternal() {
        getAccountInternal()?.let { account ->
            Log.d(TAG, "清除账户内部 - 用户名: ${account.name}")
            // 清除 Token
            val token = accountManager.peekAuthToken(account, TOKEN_TYPE)
            if (token != null) {
                accountManager.invalidateAuthToken(ACCOUNT_TYPE, token)
                Log.d(TAG, "Token已失效化")
            }
            // 删除账户
            val removed = accountManager.removeAccountExplicitly(account)
            Log.d(TAG, "账户删除结果: $removed")
        }
    }

    companion object {
        const val ACCOUNT_TYPE = "com.example.loginapp.account"
        const val TOKEN_TYPE = "Bearer"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
    }
}

/**
 * 账户操作异常
 */
class AccountException(message: String, cause: Throwable? = null) : Exception(message, cause)
