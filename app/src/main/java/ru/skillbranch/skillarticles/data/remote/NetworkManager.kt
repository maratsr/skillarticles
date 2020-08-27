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
import ru.skillbranch.skillarticles.data.remote.interceptors.NetworkStatusInterceptor
import java.util.*

object NetworkManager {
    val api: RestService by lazy {
        // Отслеживаем request/response
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        //client
        val client = OkHttpClient().newBuilder()
//            .readTimeout(2, TimeUnit.SECONDS)    // socket timeout (GET)
//            .writeTimeout(5, TimeUnit.SECONDS)   // socket timeout (POST, PUT, etc.)
            .addInterceptor(NetworkStatusInterceptor()) // intercept network status
//            .authenticator(TokenAuthenticator())
            .addInterceptor(logging)                    // log requests/results
//            .addInterceptor(ErrorStatusInterceptor())   // intercept network errors
            .build()

        //json converter
        val moshi = Moshi.Builder()
            .add(DateAdapter()) // convert long timestamp to date
            .add(KotlinJsonAdapterFactory()) // convert json to class by reflection
            .build()



        //retrofit
        val retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()

        retrofit.create(RestService::class.java)

    }
}

class DateAdapter {
    @FromJson
    fun fromJson(timestamp: Long) = Date(timestamp)

    @ToJson
    fun toJson(date: Date) = date.time
}