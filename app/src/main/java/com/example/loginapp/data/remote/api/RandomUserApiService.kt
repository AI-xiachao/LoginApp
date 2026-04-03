package com.example.loginapp.data.remote.api

import com.example.loginapp.data.remote.dto.RandomUserResponse
import retrofit2.Response
import retrofit2.http.GET

interface RandomUserApiService {

    @GET("api/")
    suspend fun getRandomUser(): Response<RandomUserResponse>
}
