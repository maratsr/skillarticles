package ru.skillbranch.skillarticles.di.modules

import android.content.Context
import com.squareup.moshi.Moshi
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
    fun providePrefManager(
        context: Context,
        moshi: Moshi): PrefManager = PrefManager(context, moshi)
}