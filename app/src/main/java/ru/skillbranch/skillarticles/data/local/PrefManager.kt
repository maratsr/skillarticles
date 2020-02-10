package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

class PrefManager(context: Context): PreferenceManager(context) {

    var storedString by PrefDelegate("")

    val preferences  : SharedPreferences by lazy {
        getDefaultSharedPreferences(context)
        //sharedPreferences
    }

    fun clearAll() {
       with (preferences.edit()) {
           clear().apply()
       }
    }
}