package com.example.loginapp.data.repository

import android.util.Log
import com.example.loginapp.data.local.RandomUserLocalDataSource
import com.example.loginapp.data.remote.api.RandomUserApiService
import com.example.loginapp.domain.model.RandomUser
import com.example.loginapp.domain.repository.RandomUserRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RandomUserRepository"

@Singleton
class RandomUserRepositoryImpl @Inject constructor(
    private val apiService: RandomUserApiService,
    private val localDataSource: RandomUserLocalDataSource
) : RandomUserRepository {

    override fun getRandomUserStream(): Flow<RandomUser?> {
        return localDataSource.getRandomUser()
    }

    override suspend fun loadFromCache(): RandomUser? {
        return localDataSource.getRandomUserSync()
    }

    override suspend fun fetchAndCache(): Result<RandomUser> {
        Log.d(TAG, "开始从网络获取随机用户...")
        return try {
            val response = apiService.getRandomUser()
            if (response.isSuccessful) {
                val body = response.body()
                val userResult = body?.results?.firstOrNull()
                if (userResult != null) {
                    val user = RandomUser(
                        fullName = "${userResult.name.first} ${userResult.name.last}",
                        email = userResult.email,
                        avatarUrl = userResult.picture.large,
                        location = "${userResult.location.city}, ${userResult.location.country}"
                    )
                    localDataSource.saveRandomUser(user)
                    Log.i(TAG, "获取并缓存随机用户成功 - 姓名: ${user.fullName}")
                    Result.success(user)
                } else {
                    Log.w(TAG, "API返回空数据")
                    Result.failure(Exception("No user data"))
                }
            } else {
                Log.e(TAG, "API请求失败 - HTTP ${response.code()}")
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Log.e(TAG, "网络异常", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "获取随机用户异常", e)
            Result.failure(e)
        }
    }

}
