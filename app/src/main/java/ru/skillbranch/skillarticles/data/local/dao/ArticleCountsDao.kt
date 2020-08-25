package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.ArticleCounts

@Dao
interface ArticleCountsDao: BaseDao<ArticleCounts> {
    @Transaction
    suspend fun upsert(list: List<ArticleCounts>) {
        insert(list)
            .mapIndexed{ index, recordResult -> if(recordResult == -1L) list[index] else null }
            .filterNotNull()
            .also{ if(it.isNotEmpty()) update(it)}
    }

    @Query("""
        select * from article_counts
    """)
    fun findArticleCounts(): LiveData<List<ArticleCounts>>


    @Query("""
        select * from article_counts where article_id = :articleId
    """)
    fun findArticleCounts(articleId: String): LiveData<ArticleCounts>

    //fun incrementLikeOrInsert(articleId: String)

    @Query("""
        update article_counts set likes=likes+1, updated_at = CURRENT_TIMESTAMP
        where article_id = :articleId
        """
    )
    suspend fun incrementLike(articleId: String) : Int

    @Query("""
        update article_counts set likes=max(0,likes-1), updated_at = CURRENT_TIMESTAMP
        where article_id = :articleId
        """
    )
    suspend fun decrementLike(articleId: String) : Int

    @Query("""
        update article_counts set comments=comments+1, updated_at = CURRENT_TIMESTAMP
        where article_id = :articleId
        """
    )
    suspend fun incrementCommentsCount(articleId: String)

    @Query("""
        select comments from article_counts where article_id = :articleId
    """)
    fun getCommentsCount(articleId: String) : LiveData<Int>

    @Query("SELECT * FROM article_contents") // для тестирования
    suspend fun findArticlesContentsTest(): List<ArticleCounts>
}