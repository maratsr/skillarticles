package ru.skillbranch.skillarticles.di.modules

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.skillbranch.skillarticles.data.local.PrefManager
import javax.inject.Singleton

// МОдуль провайдящий SharedPreferences
@Module
@InstallIn(ApplicationComponent::class) // Все нижеописанное будет установлено в ApplicationComponent
object PreferencesModule {

    //legacy style
    @Provides
    @Singleton
    fun providePrefManager(
        @ApplicationContext context: Context, // Hilt сам запровайдит контекст application в параметр
        moshi: Moshi): PrefManager = PrefManager(context, moshi)
}