package com.example.loginapp.data.local

import com.example.loginapp.domain.model.RandomUser
import kotlinx.coroutines.flow.Flow

interface RandomUserLocalDataSource {

    fun getRandomUser(): Flow<RandomUser?>

    suspend fun getRandomUserSync(): RandomUser?

    suspend fun saveRandomUser(user: RandomUser)

    suspend fun clearAll()
}
