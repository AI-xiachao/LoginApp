package com.example.loginapp.data.repository

import android.content.Context
import android.util.Log
import com.example.loginapp.data.datasource.AccountDataSource
import com.example.loginapp.data.device.OaidManager
import com.example.loginapp.data.remote.api.AuthApiService
import com.example.loginapp.domain.model.AuthState
import com.example.loginapp.domain.model.LoginCredentials
import com.example.loginapp.domain.model.LoginError
import com.example.loginapp.domain.model.LoginResult
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

/**
 * 认证仓库实现
 *
 * 通过 AccountDataSource 访问账户数据，不直接操作 AccountManager
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val accountDataSource: AccountDataSource,
    private val oaidManager: OaidManager,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var cachedUser: User? = null

    init {
        Log.d(TAG, "AuthRepository初始化完成 - 初始状态: Unauthenticated")
    }

    override suspend fun login(credentials: LoginCredentials): LoginResult {
        Log.i(TAG, "开始登录流程 - 用户名: ${credentials.username}")

        return try {
            // 获取 OAID（挂起函数，非阻塞）
            val oaid = oaidManager.getOaid()
            Log.d(TAG, "获取到 OAID: ${oaid.take(8)}...")

            // 获取应用版本
            val appVersion = getAppVersion()
            Log.d(TAG, "应用版本: $appVersion")

            // 调用真实登录API
            Log.d(TAG, "调用登录API...")
            val response = apiService.loginWithPassword(
                username = credentials.username,
                password = credentials.password,
                deviceId = oaid,
                appVersion = appVersion
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    Log.e(TAG, "登录API返回空响应体 - HTTP ${response.code()}")
                    return LoginResult.Failure(LoginError.ServerError)
                }

                // 检查业务码
                if (body.code != "200") {
                    Log.w(TAG, "登录业务失败 - code: ${body.code}, message: ${body.message}")
                    return LoginResult.Failure(
                        LoginError.Unknown(body.message)
                    )
                }

                val value = body.value
                if (value == null) {
                    Log.e(TAG, "登录成功但响应value为空")
                    return LoginResult.Failure(LoginError.ServerError)
                }

                Log.i(TAG, "登录API成功 - 用户ID: ${value.userId}, 用户名: ${value.userName}")

                // 创建 User 对象
                val user = User(
                    id = value.userId ?: "",
                    username = value.userName ?: credentials.username,
                    email = value.userName ?: "", // 使用 userName 作为 email（Flyme账号格式）
                    token = value.oauthToken ?: ""
                )
                Log.d(TAG, "创建User对象 - ID: ${user.id}, Token前缀: ${user.token.take(10)}...")

                // 通过 AccountDataSource 保存账户信息
                Log.d(TAG, "调用AccountDataSource保存账户...")
                accountDataSource.saveAccount(user).getOrElse {
                    Log.e(TAG, "保存账户到DataSource失败 - 错误: ${it.message}")
                    return LoginResult.Failure(
                        LoginError.Unknown("保存账户失败: ${it.message}")
                    )
                }
                Log.i(TAG, "账户保存成功")

                cachedUser = user
                _authState.value = AuthState.Authenticated(user)
                Log.i(TAG, "登录流程完成 - 用户已认证，状态已更新")

                LoginResult.Success(user)
            } else {
                Log.w(TAG, "登录API失败 - HTTP ${response.code()}")
                when (response.code()) {
                    401 -> {
                        Log.w(TAG, "401 Unauthorized - 无效凭证")
                        LoginResult.Failure(LoginError.InvalidCredentials)
                    }
                    in 500..599 -> {
                        Log.e(TAG, "服务器错误 - HTTP ${response.code()}")
                        LoginResult.Failure(LoginError.ServerError)
                    }
                    else -> {
                        Log.e(TAG, "未知HTTP错误 - 状态码: ${response.code()}")
                        LoginResult.Failure(
                            LoginError.Unknown("HTTP ${response.code()}")
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "登录网络异常 - 错误: ${e.message}", e)
            LoginResult.Failure(LoginError.NetworkError)
        } catch (e: HttpException) {
            Log.e(TAG, "登录HTTP异常 - 错误: ${e.message}", e)
            LoginResult.Failure(LoginError.Unknown(e.message ?: "Unknown error"))
        } catch (e: Exception) {
            Log.e(TAG, "登录未知异常", e)
            LoginResult.Failure(LoginError.Unknown(e.message ?: "Unknown error"))
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            Log.w(TAG, "获取应用版本失败", e)
            "1.0.0"
        }
    }

    override suspend fun logout(): Result<Unit> {
        Log.i(TAG, "开始登出流程...")
        val currentUser = cachedUser
        if (currentUser != null) {
            Log.d(TAG, "当前登录用户: ${currentUser.username}")
        }

        return try {
            // 调用后端登出接口（可选）
            Log.d(TAG, "调用登出API...")
            kotlin.runCatching { apiService.logout() }
                .onSuccess { Log.d(TAG, "登出API调用成功") }
                .onFailure { Log.w(TAG, "登出API调用失败(非关键): ${it.message}") }

            // 通过 AccountDataSource 清除账户
            Log.d(TAG, "调用AccountDataSource清除账户...")
            accountDataSource.clearAccount().getOrThrow()
            Log.i(TAG, "账户清除成功")

            cachedUser = null
            _authState.value = AuthState.Unauthenticated
            Log.i(TAG, "登出完成 - 状态已更新为Unauthenticated")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "登出异常", e)
            Result.failure(e)
        }
    }

    override suspend fun restoreSession(): Result<User?> {
        Log.i(TAG, "开始恢复会话...")

        return try {
            // 通过 AccountDataSource 获取账户（直接返回 User）
            Log.d(TAG, "从AccountDataSource获取账户...")
            val user = accountDataSource.getAccount()

            if (user == null) {
                Log.i(TAG, "无保存的账户信息 - 会话恢复结果为null")
                return Result.success(null)
            }

            Log.i(TAG, "获取到账户 - 用户ID: ${user.id}, 用户名: ${user.username}")
            Log.d(TAG, "Token信息 - 前缀: ${user.token.take(10)}...")

            // 验证 Token 是否有效（可选：调用后端验证接口）
            // val valid = apiService.validateToken(user.token).isSuccessful

            cachedUser = user
            _authState.value = AuthState.Authenticated(user)
            Log.i(TAG, "会话恢复成功 - 状态已更新为Authenticated")

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "恢复会话异常", e)
            // 恢复失败，清除账户
            Log.d(TAG, "恢复失败，尝试清除账户...")
            kotlin.runCatching { accountDataSource.clearAccount() }
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        Log.v(TAG, "获取当前用户 - 结果: ${cachedUser?.username ?: "null"}")
        return cachedUser
    }
}
