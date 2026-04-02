package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.AuthState
import com.example.loginapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 监听认证状态用例
 */
class CheckAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}
