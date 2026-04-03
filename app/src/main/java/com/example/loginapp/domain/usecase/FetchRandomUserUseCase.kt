package com.example.loginapp.domain.usecase

import com.example.loginapp.domain.model.RandomUser
import com.example.loginapp.domain.repository.RandomUserRepository
import javax.inject.Inject

class FetchRandomUserUseCase @Inject constructor(
    private val repository: RandomUserRepository
) {

    suspend operator fun invoke(): Result<RandomUser> = repository.fetchAndCache()
}
