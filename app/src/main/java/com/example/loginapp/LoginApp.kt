package com.example.loginapp

import android.app.Application
import android.util.Log
import com.example.loginapp.data.device.OaidManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

private const val TAG = "LoginApp"

/**
 * Application 类 - Hilt 入口点
 */
@HiltAndroidApp
class LoginApp : Application() {

    @Inject
    lateinit var oaidManager: OaidManager

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "App启动 - 初始化OAID")
        oaidManager.init()
    }
}
