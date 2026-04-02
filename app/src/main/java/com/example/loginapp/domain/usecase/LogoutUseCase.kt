package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 登出用例
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
