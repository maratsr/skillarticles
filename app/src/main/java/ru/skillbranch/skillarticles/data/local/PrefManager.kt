package ru.skillbranch.skillarticles.data.local

import android.content.Context
import androidx.preference.PreferenceManager

class PrefManager(context: Context): PreferenceManager(context) {
    val preferences = super.getSharedPreferences()
    fun clearAll() {
       with (preferences.edit()) {
           clear()
           commit()
       }
    }
}