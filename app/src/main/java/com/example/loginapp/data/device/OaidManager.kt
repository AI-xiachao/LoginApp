package com.example.loginapp.data.device

import android.content.Context
import android.provider.Settings
import android.util.Log
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

private const val TAG = "DeviceIdManager"
private const val INIT_TIMEOUT_MS = 3000L

@Singleton
class OaidManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val initDeferred = CompletableDeferred<String>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init() {
        scope.launch {
            val id = getAndroidId()
            Log.i(TAG, "Device ID initialized: ${id.take(8)}...")
            initDeferred.complete(id)
        }
    }

    suspend fun getOaid(timeoutMs: Long = INIT_TIMEOUT_MS): String = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs) {
            initDeferred.await()
        }
        result ?: ""
    }

    fun isInitialized(): Boolean = initDeferred.isCompleted

    private fun getAndroidId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""
    }
}
