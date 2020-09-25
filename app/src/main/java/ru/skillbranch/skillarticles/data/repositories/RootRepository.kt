package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.LoginReq
import ru.skillbranch.skillarticles.data.remote.req.SignUpReq
import ru.skillbranch.skillarticles.data.remote.res.AuthRes

object RootRepository {
    val preferences = PrefManager
    private val network = NetworkManager.api

    fun isAuth() : LiveData<Boolean> = preferences.isAuthLive

    suspend fun login(login: String, pass: String) {
        setupPrincipal(network.login(LoginReq(login, pass)))
    }

    suspend fun signUp(name: String, login: String, pass: String) {
        setupPrincipal(network.signUp(SignUpReq(name, login, pass)))
    }

    private fun setupPrincipal(authRes: AuthRes?) {
        authRes
            ?.let {
            preferences.profile = it.user
            // Получим токены
            preferences.accessToken = "Bearer ${it.accessToken}"
            preferences.refreshToken = it.refreshToken
        }
    }
}