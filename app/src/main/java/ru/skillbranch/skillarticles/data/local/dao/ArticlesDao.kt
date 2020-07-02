package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem

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

    @Query("""
        select * from ArticleItem
    """)
    fun findArticleItems():List<ArticleItem> {
        TODO("not implemented")
    }

    @Delete
    fun delete(article: Article)

    @Query("""
        select * from ArticleItem where category_id in (:categoryIds)
    """)
    fun findArticleItemsByCategoryIds(categoryIds: List<String>): List<ArticleItem>

    @Query("""
        select * from ArticleItem
        inner join article_tag_x_ref as refs on refs.a_id = id
        where refs.t_id =:tag
    """)
    fun findArticlesByTagId(tag: String): List<ArticleItem>
}