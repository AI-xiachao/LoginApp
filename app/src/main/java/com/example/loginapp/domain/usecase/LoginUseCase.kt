package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.LoginCredentials
import com.example.loginapp.domain.model.LoginError
import com.example.loginapp.domain.model.LoginResult
import com.example.loginapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 登录用例 - 封装登录业务逻辑
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): LoginResult {
        // 输入验证
        if (username.isBlank() || password.isBlank()) {
            return LoginResult.Failure(
                LoginError.InvalidCredentials
            )
        }

        // 自动补全 @flyme.cn
        val normalizedUsername = normalizeUsername(username.trim())

        val credentials = LoginCredentials(
            username = normalizedUsername,
            password = password
        )

        return authRepository.login(credentials)
    }

    /**
     * 自动补全 Flyme 账号后缀
     *
     * 如果用户输入不包含 @，则自动添加 @flyme.cn
     */
    private fun normalizeUsername(username: String): String {
        return if (username.contains("@")) {
            username
        } else {
            "${username}@flyme.cn"
        }
    }
}
