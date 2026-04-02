package com.example.loginapp.data.device

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.meizu.flyme.openidsdk.OpenIdHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "OaidManager"
private const val INIT_TIMEOUT_MS = 3000L

/**
 * OAID 管理器
 *
 * 负责获取设备的 OAID（Open Advertising ID）或备用设备标识
 *
 * 使用优先级：
 * 1. 魅族 OpenIdHelper
 * 2. Android ID（作为 fallback）
 *
 * 职责：
 * - App启动时异步初始化设备标识
 * - 提供挂起函数获取设备标识（非阻塞）
 */
@Singleton
class OaidManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val initDeferred = CompletableDeferred<String>()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    /**
     * 异步初始化设备标识
     *
     * 应在 Application.onCreate 中调用
     */
    fun init() {
        scope.launch {
            val id = try {
                Log.d(TAG, "开始初始化设备标识...")
                // 优先尝试 OpenIdHelper，失败时使用 Android ID
                getOpenId() ?: getAndroidId()
            } catch (e: Exception) {
                Log.e(TAG, "设备标识初始化失败，使用 Android ID", e)
                getAndroidId()
            }
            Log.i(TAG, "设备标识初始化完成: ${id.take(8)}...")
            initDeferred.complete(id)
        }
    }

    /**
     * 挂起函数获取设备标识
     *
     * 非阻塞，使用超时机制
     *
     * @param timeoutMs 超时时间（毫秒），默认3秒
     * @return 设备标识字符串，失败返回空字符串
     */
    suspend fun getOaid(timeoutMs: Long = INIT_TIMEOUT_MS): String = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs) {
            initDeferred.await()
        }
        if (result == null) {
            Log.w(TAG, "获取设备标识超时，返回空字符串")
        }
        result ?: ""
    }

    /**
     * 检查设备标识是否已初始化完成
     */
    fun isInitialized(): Boolean = initDeferred.isCompleted

    /**
     * 通过 OpenIdHelper 获取 OAID
     */
    private fun getOpenId(): String? {
        return try {
            // OpenIdHelper 使用静态方法，没有回调
            OpenIdHelper.getOAID(context)
        } catch (e: Exception) {
            Log.w(TAG, "OpenIdHelper 获取失败: ${e.message}")
            null
        }
    }

    /**
     * 获取 Android ID 作为备用方案
     */
    private fun getAndroidId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""
    }
}
