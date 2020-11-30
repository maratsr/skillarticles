package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.req.LoginReq
import ru.skillbranch.skillarticles.data.remote.req.SignUpReq
import ru.skillbranch.skillarticles.data.remote.res.AuthRes
import javax.inject.Inject

class RootRepository
    @Inject constructor( // Пометили @Inject чтобы Dagger мог впрыснуть зависимости PrefManager, RestService.
        // Если понадобится RootRepository то Dagger знает как его создать с нужными зависимостями
    private val preferences: PrefManager,
    private val network: RestService,
) : IRepository {


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