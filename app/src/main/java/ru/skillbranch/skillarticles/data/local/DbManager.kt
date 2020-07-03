package ru.skillbranch.skillarticles.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.BuildConfig
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.*

object DbManager {
    val db = Room.databaseBuilder(
        App.applicationContext(),
        AppDb::class.java,
        AppDb.DATABASE_NAME
    ).build()
}

// Создадим БД приложения
@Database(
    entities = [
        Article::class,
        ArticleCounts::class,
        Category::class,
        ArticlePersonalInfo::class,
        Tag::class,
        ArticleTagXRef::class,
        ArticleContent::class
    ],
    version = AppDb.DATABASE_VERSION,
    exportSchema = false,
    views = [ArticleItem::class]

)

@TypeConverters(DateConverter::class)
abstract class AppDb: RoomDatabase() {
    companion object {
        const val DATABASE_NAME: String = BuildConfig.APPLICATION_ID + ".db"
        const val DATABASE_VERSION = 1
    }

    abstract fun articlesDao(): ArticlesDao
    abstract fun articleCountsDao(): ArticleCountsDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun articalPersonalInfos(): ArticlePersonalInfosDao
    abstract fun tagsDao(): TagsDao
    abstract fun articleContentsDao(): ArticleContentsDao
}