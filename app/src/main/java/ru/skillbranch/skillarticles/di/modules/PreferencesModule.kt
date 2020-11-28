package ru.skillbranch.skillarticles.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.skillbranch.skillarticles.data.local.PrefManager
import javax.inject.Singleton

// МОдуль провайдяший SharedPreferences
@Module
object PreferencesModule {

    //legacy style
    @Provides
    @Singleton
    fun providePrefManager(context: Context): PrefManager = PrefManager(context)
}