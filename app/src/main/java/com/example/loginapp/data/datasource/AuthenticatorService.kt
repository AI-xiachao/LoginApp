package com.example.loginapp.data.datasource

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * AccountManager 要求绑定的 Service
 *
 * 注意：虽然使用了 AccountManager，但所有账户操作都通过 AccountDataSource 封装，
 * 该 Service 仅为满足 Android AccountManager 框架要求而保留。
 */
class AuthenticatorService : Service() {

    private lateinit var authenticator: AccountAuthenticator

    override fun onCreate() {
        super.onCreate()
        authenticator = AccountAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return authenticator.iBinder
    }
}
