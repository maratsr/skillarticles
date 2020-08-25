package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {

    internal val preferences : SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }


//    var storedBoolean by PrefDelegate(false)
//    var storedString by PrefDelegate("test")
//    var storedInt by PrefDelegate(Int.MAX_VALUE)
//    var storedLong by PrefDelegate(Long.MAX_VALUE)
//    var storedFloat by PrefDelegate(100f)

    var isDarkMode by PrefDelegate(false)
    var isBigText by PrefDelegate(false)
    var isAuth by PrefDelegate(false)
    val isAuthLive: LiveData<Boolean> by PrefLiveDelegate("isAuth", false,  preferences)

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

    fun clearAll(){
        preferences.edit().clear().apply()
    }


    fun isAuth(): MutableLiveData<Boolean> {
        return isAuth()

    }

    fun setAuth(auth: Boolean): Unit {
        isAuth = auth
    }
}