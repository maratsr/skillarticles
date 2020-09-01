package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.ArticleContent

@Dao
interface ArticleContentsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obj: ArticleContent): Long // Возвращает RowID или -1 если ошибка

    @Query("select * from article_contents") // для тестирования
    suspend fun findArticlesContentsTest(): List<ArticleContent>

    @Query("delete from article_contents where article_id = :articleId")
    suspend fun deleteById(articleId: String)
}