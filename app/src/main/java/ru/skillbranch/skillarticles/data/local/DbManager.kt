package ru.skillbranch.skillarticles.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.BuildConfig
import ru.skillbranch.skillarticles.data.local.dao.ArticlesDao
import ru.skillbranch.skillarticles.data.local.entities.Article

object DbManager {
    val db = Room.databaseBuilder(
        App.applicationContext(),
        AppDb::class.java,
        AppDb.DATABASE_NAME
    ).build()
}

// Создадим БД приложения
@Database(
    entities = [Article::class],
    version = AppDb.DATABASE_VERSION,
    exportSchema = false,
    views = []

)
abstract class AppDb: RoomDatabase() {
    companion object {
        const val DATABASE_NAME: String = BuildConfig.APPLICATION_ID + ".db"
        const val DATABASE_VERSION = 1
    }

    abstract fun articlesDao(): ArticlesDao
}