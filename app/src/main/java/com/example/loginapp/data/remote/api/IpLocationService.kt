package com.example.loginapp.data.remote.api

import com.example.loginapp.data.remote.dto.IpLocationResponse
import retrofit2.Response
import retrofit2.http.GET

interface IpLocationService {

    /**
     * 根据 IP 获取粗略位置信息
     */
    @GET("/")
    suspend fun getLocationByIp(): Response<IpLocationResponse>

    companion object {
        const val BASE_URL = "https://ipwho.is/"
    }
}
