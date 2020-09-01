package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.JsonConverter.moshi
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveObjDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefObjDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings
import ru.skillbranch.skillarticles.data.models.User

object PrefManager {
    internal val preferences : SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    var isDarkMode by PrefDelegate(false)
    var isBigText by PrefDelegate(false)
    var accessToken by PrefDelegate("")
    var refreshToken by PrefDelegate("")

    // moshi из JsonCoverter объекта
    var profile: User? by PrefObjDelegate(moshi.adapter(User::class.java)) // Делегат умеющий созранять nullable

    val isAuthLive by lazy { // Если токен = null вернет isAuthLive=false, иначе = true
        val token by PrefLiveDelegate("accessToken", "", preferences)
        token.map { it.isNotEmpty() }
    }

    // Здесь используется рефлексия для адаптирования data class <-> Json
    val profileLive by PrefLiveObjDelegate("profile", moshi.adapter(User::class.java), preferences)

    // Используя kapt "com.squareup.moshi:moshi-kotlin-codegen + BuildProject сгенерируется код UserJsonAdapter если в
    // использовать аннотацию @JsonClass(generateAdapter = true) data class User
    //val profileLive: LiveData<User?> by PrefLiveObjDelegate("profile", UserJsonAdapter(moshi) as JsonAdapter<User?>, preferences)

    val appSettings = MediatorLiveData<AppSettings>().apply {
        val isDarkModeLive by PrefLiveDelegate("isDarkMode", false, preferences)
        val isBigTextLive by PrefLiveDelegate("isBigText", false, preferences)
        value = AppSettings()

        addSource(isDarkModeLive) {
            value = value!!.copy(isDarkMode = it)
        }

        addSource(isBigTextLive) {
            value = value!!.copy(isBigText = it)
        }
    }.distinctUntilChanged() // Возвращает новые значения если они только изменятся

    fun setAppSettings(appSettings: AppSettings) {
        isDarkMode = appSettings.isDarkMode
        isBigText = appSettings.isBigText
    }
}