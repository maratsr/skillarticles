package ru.skillbranch.skillarticles.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.skillbranch.skillarticles.data.remote.NetworkMonitor
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class) // Все нижеописанное будет установлено в ApplicationComponent
object NetworkUtilsModule{
    @Provides
    @Singleton
    // Hilt сам запровайдит контекст application в параметр
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor = NetworkMonitor(context)
}