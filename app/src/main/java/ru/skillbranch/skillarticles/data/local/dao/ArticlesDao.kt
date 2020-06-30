package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.Article

@Dao
interface ArticlesDao: BaseDao<Article> {

    @Transaction
    fun upsert(list: List<Article>) {
        insert(list)
            .mapIndexed{ index, recordResult -> if(recordResult == -1L) list[index] else null }
            .filterNotNull()
            .also{ if(it.isNotEmpty()) update(it)}
    }

    @Query("""
        select * from articles
    """)
    fun findArticles(): List<Article>
    @Query("""
        select * from articles where id=:id
    """)
    fun findArticleById(id: String): Article
}