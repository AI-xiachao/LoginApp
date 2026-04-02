package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * App 启动时恢复会话用例
 */
class RestoreSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return authRepository.restoreSession()
    }
}
