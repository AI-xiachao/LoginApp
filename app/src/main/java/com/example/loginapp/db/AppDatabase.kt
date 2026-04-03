package com.example.loginapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.loginapp.db.dao.RandomUserDao
import com.example.loginapp.db.dao.WeatherCacheDao
import com.example.loginapp.db.entity.RandomUserEntity
import com.example.loginapp.db.entity.WeatherCacheEntity

@Database(
    entities = [RandomUserEntity::class, WeatherCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun randomUserDao(): RandomUserDao
    abstract fun weatherCacheDao(): WeatherCacheDao
}
