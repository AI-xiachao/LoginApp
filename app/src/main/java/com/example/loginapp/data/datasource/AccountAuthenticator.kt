package com.example.loginapp.data.datasource

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle

/**
 * AccountAuthenticator - Android AccountManager 要求的认证器实现
 *
 * 用于系统账户管理界面与 App 账户交互。
 * 实际登录逻辑在 UI 层处理，这里仅提供基本的框架实现。
 */
class AccountAuthenticator(
    private val context: Context
) : AbstractAccountAuthenticator(context) {

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle = Bundle()

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle = Bundle()

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val accountManager = AccountManager.get(context)
        val authToken = accountManager.peekAuthToken(account, authTokenType)

        return if (authToken != null) {
            Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        } else {
            Bundle().apply {
                putString(AccountManager.KEY_ERROR_MESSAGE, "No auth token available")
            }
        }
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle = Bundle().apply {
        putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
    }

    override fun getAuthTokenLabel(authTokenType: String?): String? = null

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? = null

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? = null
}
