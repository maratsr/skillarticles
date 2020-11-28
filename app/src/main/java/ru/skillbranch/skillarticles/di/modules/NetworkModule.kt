package ru.skillbranch.skillarticles.di.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.skillbranch.skillarticles.AppConfig.BASE_URL
import ru.skillbranch.skillarticles.data.JsonConverter
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkMonitor
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.interceptors.ErrorStatusInterceptor
import ru.skillbranch.skillarticles.data.remote.interceptors.NetworkStatusInterceptor
import ru.skillbranch.skillarticles.data.remote.interceptors.TokenAuthenticator
import java.util.concurrent.TimeUnit

@Module
object NetworkModule {

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    fun provideRestService(retrofit: Retrofit): RestService = retrofit.create(RestService::class.java)

    @Provides
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    @Provides
    fun provideOkHttpClient(
        tokenAuthenticator: TokenAuthenticator,
        networkStatusInterceptor: NetworkStatusInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        errorStatusInterceptor: ErrorStatusInterceptor
    ): OkHttpClient = OkHttpClient().newBuilder()
        .readTimeout(2, TimeUnit.SECONDS)    // socket timeout (GET)
        .writeTimeout(5, TimeUnit.SECONDS)   // socket timeout (POST, PUT, etc.)
        .authenticator(tokenAuthenticator)        // попытаться получить новый access токен через refresh токен
        .addInterceptor(networkStatusInterceptor) // intercept network status
        .addInterceptor(httpLoggingInterceptor)                    // log requests/results
        .addInterceptor(errorStatusInterceptor)   // intercept network errors
        .build()

    @Provides
     fun provideNetworkStatusInterceptor(): NetworkStatusInterceptor = NetworkStatusInterceptor()

}