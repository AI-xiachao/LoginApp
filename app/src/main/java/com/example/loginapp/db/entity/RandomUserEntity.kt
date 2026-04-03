package com.example.loginapp.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "random_users")
data class RandomUserEntity(
    @PrimaryKey
    val id: String = "current",
    val fullName: String,
    val email: String,
    val avatarUrl: String,
    val location: String,
    val updatedAt: Long = System.currentTimeMillis()
)
