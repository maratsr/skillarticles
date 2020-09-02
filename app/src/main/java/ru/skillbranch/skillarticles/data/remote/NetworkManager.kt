package ru.skillbranch.skillarticles.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.skillbranch.skillarticles.AppConfig.BASE_URL
import ru.skillbranch.skillarticles.data.JsonConverter.moshi
import ru.skillbranch.skillarticles.data.remote.interceptors.ErrorStatusInterceptor
import ru.skillbranch.skillarticles.data.remote.interceptors.NetworkStatusInterceptor
import ru.skillbranch.skillarticles.data.remote.interceptors.TokenAuthenticator
import java.util.*
import java.util.concurrent.TimeUnit

object NetworkManager {
    val api: RestService by lazy {
        // Отслеживаем request/response
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        //client
        val client = OkHttpClient().newBuilder()
            .readTimeout(2, TimeUnit.SECONDS)    // socket timeout (GET)
            .writeTimeout(5, TimeUnit.SECONDS)   // socket timeout (POST, PUT, etc.)
            .addInterceptor(NetworkStatusInterceptor()) // intercept network status
            .addInterceptor(logging)                    // log requests/results
            .authenticator(TokenAuthenticator()) // попытаться получить новый access токен через refresh токен
            .addInterceptor(ErrorStatusInterceptor())   // intercept network errors
            .build()

        //retrofit
        val retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()

        retrofit.create(RestService::class.java)

    }
}