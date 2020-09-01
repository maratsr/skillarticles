package ru.skillbranch.skillarticles.data.remote.interceptors

import android.util.Log
import com.squareup.moshi.JsonEncodingException
import okhttp3.Interceptor
import okhttp3.Response
import ru.skillbranch.skillarticles.data.JsonConverter.moshi
import ru.skillbranch.skillarticles.data.remote.err.ApiError
import ru.skillbranch.skillarticles.data.remote.err.ErrorBody

// Перехватывает response от сервера и отдает их как ошибку
class ErrorStatusInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val res = chain.proceed(chain.request())

        if (res.isSuccessful) return res

        val errMessage = try {
            moshi.adapter(ErrorBody::class.java).fromJson(res.body!!.string())?.message
        } catch (e: JsonEncodingException) {
            e.message
        }

        when (res.code) { // Кидаем ошибку в зависимости от response фронт-сервера
            400 -> throw ApiError.BadRequest(errMessage)
            401 -> {
                Log.d("Not Found", " 401 Intercepted")
                throw ApiError.Unauthorized(errMessage)
            }
            403 -> throw ApiError.Forbidden(errMessage)
            404 -> throw ApiError.NotFound(errMessage)
            500 -> throw ApiError.InternalServerError(errMessage)
            else -> throw ApiError.UnknownError(errMessage)
        }
    }
}