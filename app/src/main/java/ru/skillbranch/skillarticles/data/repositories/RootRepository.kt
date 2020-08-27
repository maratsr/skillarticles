package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.LoginReq

object RootRepository {
    val preferences = PrefManager
    private val network = NetworkManager.api

    fun isAuth() : LiveData<Boolean> = preferences.isAuth()
    fun setAuth(auth:Boolean) = preferences.setAuth(auth)
    suspend fun login(login: String, pass: String) {
        // Получим токены
        val auth = network.login(LoginReq(login, pass))

        auth.user?.let { preferences.profile = it }
        preferences.accessToken = "Bearer ${auth.accessToken}"
        preferences.refreshToken = auth.refreshToken

    }
}