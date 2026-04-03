package com.example.loginapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loginapp.db.entity.RandomUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RandomUserDao {

    @Query("SELECT * FROM random_users WHERE id = 'current' LIMIT 1")
    fun getRandomUser(): Flow<RandomUserEntity?>

    @Query("SELECT * FROM random_users WHERE id = 'current' LIMIT 1")
    suspend fun getRandomUserSync(): RandomUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRandomUser(entity: RandomUserEntity)

    @Query("DELETE FROM random_users")
    suspend fun clearAll()
}
