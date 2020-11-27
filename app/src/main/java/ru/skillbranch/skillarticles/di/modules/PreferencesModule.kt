package ru.skillbranch.skillarticles.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.skillbranch.skillarticles.data.local.PrefManager

// МОдуль провайдяший SharedPreferences
@Module
class PreferencesModule(val context: Context) {

    //legacy style
    @Provides
    fun providePrefManager(): PrefManager = PrefManager(context)
}