package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
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
    fun findArticles(): LiveData<List<Article>>

    @Query("""
        select * from articles where id=:id
    """)
    fun findArticleById(id: String): LiveData<Article>

    @Query("""
        select * from ArticleItem
    """)
    fun findArticleItems():LiveData<List<ArticleItem>>

    @Delete
    fun delete(article: Article)

    @Query("""
        select * from ArticleItem where category_id in (:categoryIds)
    """)
    fun findArticleItemsByCategoryIds(categoryIds: List<String>): LiveData<List<ArticleItem>>

    @Query("""
        select * from ArticleItem
        inner join article_tag_x_ref as refs on refs.a_id = id
        where refs.t_id =:tag
    """)
    fun findArticlesByTagId(tag: String): LiveData<List<ArticleItem>>

    // указываем наблюдаемую сущность - если она обновляется то и RecyclerView автоматически подхватит
    @RawQuery(observedEntities =[ArticleItem::class])
    fun findArticlesByRaw(simpleSQLiteQuery: SimpleSQLiteQuery): DataSource.Factory<Int, ArticleItem>

    @Query("""
        select * from ArticleFull where id=:articleId
    """)
    fun findFullArticle(articleId: String): LiveData<ArticleFull>
}