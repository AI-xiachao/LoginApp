package com.example.loginapp.data.di

import android.content.Context
import androidx.room.Room
import com.example.loginapp.data.datasource.AccountDataSource
import com.example.loginapp.data.datasource.AccountDataSourceImpl
import com.example.loginapp.data.local.RandomUserLocalDataSource
import com.example.loginapp.data.local.RandomUserLocalDataSourceImpl
import com.example.loginapp.data.remote.api.AuthApiService
import com.example.loginapp.data.remote.api.IpLocationService
import com.example.loginapp.data.remote.api.MockAuthApiService
import com.example.loginapp.data.remote.api.RandomUserApiService
import com.example.loginapp.data.remote.api.WeatherApiService
import com.example.loginapp.data.repository.AuthRepositoryImpl
import com.example.loginapp.data.repository.RandomUserRepositoryImpl
import com.example.loginapp.data.repository.WeatherRepositoryImpl
import com.example.loginapp.db.AppDatabase
import com.example.loginapp.domain.repository.AuthRepository
import com.example.loginapp.domain.repository.RandomUserRepository
import com.example.loginapp.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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

    @Binds
    @Singleton
    abstract fun bindRandomUserRepository(
        impl: RandomUserRepositoryImpl
    ): RandomUserRepository

    @Binds
    @Singleton
    abstract fun bindRandomUserLocalDataSource(
        impl: RandomUserLocalDataSourceImpl
    ): RandomUserLocalDataSource

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository

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

        @Provides
        @Singleton
        fun provideRandomUserApiService(): RandomUserApiService {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://randomuser.me/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RandomUserApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "login_app_database"
            ).fallbackToDestructiveMigration()
            .build()
        }

        @Provides
        @Singleton
        fun provideRandomUserDao(database: AppDatabase) = database.randomUserDao()

        @Provides
        @Singleton
        fun provideWeatherCacheDao(database: AppDatabase) = database.weatherCacheDao()

        @Provides
        @Singleton
        fun provideWeatherApiService(okHttpClient: OkHttpClient): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(WeatherApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideIpLocationService(okHttpClient: OkHttpClient): IpLocationService {
            return Retrofit.Builder()
                .baseUrl(IpLocationService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IpLocationService::class.java)
        }
    }
}
