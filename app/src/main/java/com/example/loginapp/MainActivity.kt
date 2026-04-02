package com.example.loginapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.loginapp.domain.usecase.RestoreSessionUseCase
import com.example.loginapp.presentation.navigation.AppNavigation
import com.example.loginapp.presentation.navigation.Screen
import com.example.loginapp.presentation.theme.LoginAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivity"

/**
 * MainActivity - App 入口
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var restoreSessionUseCase: RestoreSessionUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "App启动 - 开始恢复会话")

        enableEdgeToEdge()

        // App 启动时恢复会话
        lifecycleScope.launch {
            Log.d(TAG, "调用RestoreSessionUseCase恢复会话...")
            val result = restoreSessionUseCase()

            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        Log.i(TAG, "会话恢复成功 - 用户: ${user.username}, 跳转首页")
                    } else {
                        Log.i(TAG, "无保存的会话 - 跳转登录页")
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "会话恢复失败 - 错误: ${error.message}", error)
                }
            )

            val startDestination = if (result.isSuccess && result.getOrNull() != null) {
                Screen.Home.route
            } else {
                Screen.Login.route
            }

            setContent {
                LoginAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(startDestination = startDestination)
                    }
                }
            }
        }
    }
}
