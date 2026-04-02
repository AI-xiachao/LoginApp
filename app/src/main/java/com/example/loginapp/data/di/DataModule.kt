package com.example.loginapp.data.di

import com.example.loginapp.data.datasource.AccountDataSource
import com.example.loginapp.data.datasource.AccountDataSourceImpl
import com.example.loginapp.data.remote.api.AuthApiService
import com.example.loginapp.data.remote.api.MockAuthApiService
import com.example.loginapp.data.repository.AuthRepositoryImpl
import com.example.loginapp.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Data 层依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAccountDataSource(
        impl: AccountDataSourceImpl
    ): AccountDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    companion object {

        private const val BASE_URL = "https://ologin.flyme.cn"
        private const val CONNECT_TIMEOUT = 10L
        private const val READ_TIMEOUT = 15L

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideAuthApiService(): AuthApiService {
            return MockAuthApiService()
        }
    }
}
