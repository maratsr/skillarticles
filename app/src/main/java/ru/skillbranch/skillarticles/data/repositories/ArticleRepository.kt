package ru.skillbranch.skillarticles.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.models.*
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.extensions.data.toArticleContent
import java.lang.Thread.sleep
import kotlin.math.abs

// Singleton репозиторий - эмуляция получения данных

interface IArticleRepository {
    fun findArticle(articleId: String): LiveData<ArticleFull> //
    fun getAppSettings(): LiveData<AppSettings> //
    suspend fun toggleLike(articleId: String) : Boolean //
    suspend fun toggleBookmark(articleId: String) : Boolean //
    fun isAuth(): LiveData<Boolean> //
    suspend fun sendMessage(articleId: String, message: String, answerToMessageId: String?) //
    fun loadAllComments(articleId: String, totalCount: Int, errHandler: (Throwable) -> Unit): CommentsDataFactory //
    suspend fun decrementLike(articleId: String) //
    suspend fun incrementLike(articleId: String) //
    fun updateSettings(copy: AppSettings) //
    suspend fun fetchArticleContent(articleId: String) //
    fun findArticleCommentCount(articleId: String): LiveData<Int> //
}

object ArticleRepository : IArticleRepository{
    private val network = NetworkManager.api
    private val preferences = PrefManager

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var articlesDao = db.articlesDao()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var articlePersonalDao = db.articlePersonalInfosDao()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var articleCountsDao = db.articleCountsDao()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var articleContentDao = db.articleContentsDao()

    fun setupTestDao(
        articlesDao: ArticlesDao,
        articleCountsDao: ArticleCountsDao,
        articleContentDao: ArticleContentsDao,
        articlePersonalDao: ArticlePersonalInfosDao
    ) {
        this.articlesDao = articlesDao
        this.articleCountsDao = articleCountsDao
        this.articleContentDao = articleContentDao
        this.articlePersonalDao = articlePersonalDao
    }


    override fun findArticle(articleId: String): LiveData<ArticleFull> {
        return articlesDao.findFullArticle(articleId)
    }

    override fun getAppSettings(): LiveData<AppSettings> = preferences.appSettings //from preferences

    override suspend fun toggleLike(articleId: String): Boolean {
        return articlePersonalDao.toggleLikeOrInsert(articleId)
//        return if (articlePersonalDao.toggleLikeOrInsert(articleId)) {
//            //incrementLike(articleId)
//            true
//        } else {
//            //decrementLike(articleId)
//            false
//        }
    }

    override suspend fun toggleBookmark(articleId: String)  : Boolean{
        //return articlePersonalDao.toggleBookmarkOrInsert(articleId)
        return if (articlePersonalDao.isBookmarked(articleId)) {
            //removeBookmark(articleId)
            false
        } else {
            //addBookmark(articleId)
            true
        }
    }

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId).apply { sleep(1500)}
        articleContentDao.insert(content.toArticleContent())
    }

    override fun findArticleCommentCount(articleId: String): LiveData<Int> {
        return articleCountsDao.getCommentsCount(articleId)
    }


    override fun updateSettings(appSettings: AppSettings) { // call prefManager
    }


    override suspend fun decrementLike(articleId: String) {
        articleCountsDao.decrementLike(articleId)
    }

    override suspend fun incrementLike(articleId: String) {
        articleCountsDao.incrementLike(articleId)
    }

    override fun isAuth(): LiveData<Boolean> = preferences.isAuthLive

    override suspend fun sendMessage(articleId: String, comment: String, answerToMessageId: String?) {
       /* network.sendMessage(
            articleId, comment, answerToSlug,
            User("777", "John Doe", "https://skill-branch.ru/img/mail/bot/android-category.png")
        )
        //preferences.incrementCommentsCount(articleId)
        articleCountsDao.incrementCommentsCount(articleId)*/
    }

    override fun loadAllComments(articleId: String, totalCount: Int, errHandler: (Throwable) -> Unit)=
        CommentsDataFactory(
            itemProvider = network,
            articleId = articleId,
            totalCount = totalCount,
            errHandler = errHandler
        )

    suspend fun refreshCommentsCount(articleId: String) {
        val counts = network.loadArticleCounts(articleId)
        articleCountsDao.updateCommentsCount(articleId, counts.comments)
    }

}

// Комментарии к статье
class CommentsDataFactory(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int, // кол-во комментариев
    private val errHandler: (Throwable) -> Unit
) : DataSource.Factory<String?, CommentRes>() {
    override fun create(): DataSource<String?, CommentRes> =
        CommentsDataSource(itemProvider, articleId, totalCount, errHandler)
}

class CommentsDataSource(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int,
    private val errHandler: (Throwable) -> Unit
) : ItemKeyedDataSource<String, CommentRes>() {

    // Загрузка первоначальных значений
    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<CommentRes>
    ) {
        // синхронный вызов
        try {
            val result = itemProvider.loadComments(
                articleId, params.requestedInitialKey, params.requestedLoadSize
            ).execute()

            callback.onResult(
                if (totalCount > 0) result.body()!! else emptyList(),
                0,
                totalCount
            )
        } catch (e:Throwable) {
            errHandler(e)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
        try {
            val result = itemProvider.loadComments(
                articleId,  params.key, params.requestedLoadSize).execute()
            callback.onResult(result.body()!!)
        } catch (e: Throwable) {
            errHandler(e)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
        try {
            val result = itemProvider.loadComments(
                articleId, params.key, -params.requestedLoadSize).execute()
            callback.onResult(result.body()!!)
        } catch (e: Throwable) {
            errHandler(e)
        }
    }

    override fun getKey(item: CommentRes): String = item.id

}
