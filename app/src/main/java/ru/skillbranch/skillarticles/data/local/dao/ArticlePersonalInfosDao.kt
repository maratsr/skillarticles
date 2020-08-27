package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticlePersonalInfo

@Dao
interface ArticlePersonalInfosDao: BaseDao<ArticlePersonalInfo> {

    @Transaction
    suspend fun upsert(list: List<ArticlePersonalInfo>) {
        insert(list)
            .mapIndexed{ index, recordResult -> if(recordResult == -1L) list[index] else null }
            .filterNotNull()
            .also{ if(it.isNotEmpty()) update(it)}
    }

    @Query("""
        update article_personal_infos set is_like = not is_like, updated_At=CURRENT_TIMESTAMP
        where article_id = :articleId
    """)
    suspend fun toggleLike(articleId: String): Int

    @Query("""
        update article_personal_infos set is_bookmark = not is_bookmark, updated_At=CURRENT_TIMESTAMP
        where article_id = :articleId
    """)
    suspend fun toggleBookmark(articleId: String): Int

    @Transaction
    suspend fun toggleLikeOrInsert(articleId: String): Boolean  {
        if(toggleLike(articleId)==0) {
            insert(ArticlePersonalInfo(articleId = articleId, isLike = true))
            return true
        }
        return isLiked(articleId)
    }

    @Transaction
    suspend fun toggleBookmarkOrInsert(articleId: String): Boolean {
        if(toggleBookmark(articleId)==0) insert(ArticlePersonalInfo(articleId = articleId, isBookmark = true))
        return isBookmarked(articleId)
    }

    @Query(
        """
        SELECT is_bookmark
        FROM article_personal_infos
        WHERE article_id = :articleId        
        LIMIT 1
    """
    )
    suspend fun isBookmarked(articleId: String): Boolean

    @Query(
        """
        SELECT is_like
        FROM article_personal_infos
        WHERE article_id = :articleId
        LIMIT 1
    """
    )
    fun isLiked(articleId: String): Boolean

    @Query("""
        select * from article_personal_infos
    """)
    fun findPersonalInfos(): LiveData<List<ArticlePersonalInfo>>

    @Query("""
        select * from article_personal_infos where article_id = :articleId 
    """)
    fun findPersonalInfos(articleId: String): LiveData<ArticlePersonalInfo>

    @Query("SELECT * FROM article_personal_infos WHERE article_id = :articleId") // Для тестирования
    suspend fun findPersonalInfosTest(articleId: String): ArticlePersonalInfo
}