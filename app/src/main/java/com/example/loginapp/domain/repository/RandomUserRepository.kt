package com.example.loginapp.domain.repository

import com.example.loginapp.domain.model.RandomUser
import kotlinx.coroutines.flow.Flow

interface RandomUserRepository {

    fun getRandomUserStream(): Flow<RandomUser?>

    suspend fun loadFromCache(): RandomUser?

    suspend fun fetchAndCache(): Result<RandomUser>

}
