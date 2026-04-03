package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.RandomUser
import com.example.loginapp.domain.repository.RandomUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRandomUserUseCase @Inject constructor(
    private val repository: RandomUserRepository
) {

    operator fun invoke(): Flow<RandomUser?> = repository.getRandomUserStream()
}
