package com.example.loginapp.data.local

import android.util.Log
import com.example.loginapp.db.dao.RandomUserDao
import com.example.loginapp.db.entity.RandomUserEntity
import com.example.loginapp.domain.model.RandomUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RandomUserLocalDS"

@Singleton
class RandomUserLocalDataSourceImpl @Inject constructor(
    private val randomUserDao: RandomUserDao
) : RandomUserLocalDataSource {

    override fun getRandomUser(): Flow<RandomUser?> {
        return randomUserDao.getRandomUser().map { entity ->
            Log.d(TAG, "缓存数据变化 - ${entity?.fullName ?: "null"}")
            entity?.toDomain()
        }
    }

    override suspend fun getRandomUserSync(): RandomUser? {
        val user = randomUserDao.getRandomUserSync()?.toDomain()
        Log.d(TAG, "同步查询缓存 - ${user?.fullName ?: "null"}")
        return user
    }

    override suspend fun saveRandomUser(user: RandomUser) {
        Log.i(TAG, "保存到缓存 - ${user.fullName}")
        randomUserDao.saveRandomUser(user.toEntity())
    }

    override suspend fun clearAll() {
        Log.w(TAG, "清空缓存")
        randomUserDao.clearAll()
    }

    private fun RandomUserEntity.toDomain(): RandomUser {
        return RandomUser(
            fullName = fullName,
            email = email,
            avatarUrl = avatarUrl,
            location = location
        )
    }

    private fun RandomUser.toEntity(): RandomUserEntity {
        return RandomUserEntity(
            fullName = fullName,
            email = email,
            avatarUrl = avatarUrl,
            location = location
        )
    }
}
