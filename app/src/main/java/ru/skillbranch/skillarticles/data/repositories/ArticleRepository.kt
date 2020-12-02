package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.models.*
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.err.NoNetworkError
import ru.skillbranch.skillarticles.data.remote.req.MessageReq
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.extensions.data.toArticleContent
import java.lang.Thread.sleep
import javax.inject.Inject

// Singleton репозиторий - эмуляция получения данных

interface IArticleRepository: IRepository {
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

class ArticleRepository @Inject constructor(
    private val network: RestService,
    private val preferences: PrefManager,
    private val articleDao: ArticlesDao,
    private val articlePersonalDao: ArticlePersonalInfosDao,
    private val articleCountsDao: ArticleCountsDao,
    private val articleContentDao: ArticleContentsDao
) : IArticleRepository {

    override fun findArticle(articleId: String): LiveData<ArticleFull> = articleDao.findFullArticle(articleId)

    override fun getAppSettings(): LiveData<AppSettings> = preferences.appSettings

    override suspend fun toggleLike(articleId: String): Boolean = articlePersonalDao.toggleLikeOrInsert(articleId)
    // dec/inc remove/add делать в toogleLike/Bookmark...
    override suspend fun toggleBookmark(articleId: String)  : Boolean = articlePersonalDao.isBookmarked(articleId)

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId)
            .apply { sleep(1500)}
        articleContentDao.insert(content.toArticleContent())
    }

    override fun findArticleCommentCount(articleId: String): LiveData<Int> = articleCountsDao.getCommentsCount(articleId)

    // call prefManager
    override fun updateSettings(appSettings: AppSettings) = preferences.setAppSettings(appSettings)

    override suspend fun decrementLike(articleId: String) {
        if(preferences.accessToken.isEmpty()) {
            articleCountsDao.decrementLike(articleId)
            return
        }

        try {
            val res = network.decrementLike(articleId, preferences.accessToken)
            articleCountsDao.updateLike(articleId, res.likeCount)
        } catch (e: NoNetworkError) {
            articleCountsDao.decrementLike(articleId)
        } catch (e: Throwable) {
            throw e
        }
    }

    override suspend fun incrementLike(articleId: String) {
        if(preferences.accessToken.isEmpty()) {
            articleCountsDao.incrementLike(articleId)
            return
        }

        try {
            val res = network.incrementLike(articleId, preferences.accessToken)
            articleCountsDao.updateLike(articleId, res.likeCount)
        } catch (e: NoNetworkError) {
            articleCountsDao.incrementLike(articleId)
        } catch (e: Throwable) {
            throw e
        }
    }

    override fun isAuth(): LiveData<Boolean> = preferences.isAuthLive

    override suspend fun sendMessage(articleId: String, message: String, answerToMessageId: String?) {
        val (_, messageCount) = network.sendMessage(
            articleId, MessageReq(message,  answerToMessageId), preferences.accessToken
        )
        //preferences.incrementCommentsCount(articleId)
        articleCountsDao.updateCommentsCount(articleId, messageCount)
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

    suspend fun addBookmark(articleId: String) {
        articlePersonalDao.addToBookmark(articleId)
        if(preferences.accessToken.isEmpty()) {
            return
        }

        try {
            network.addBookmark(articleId, preferences.accessToken)
        } catch (e: NoNetworkError) {
            return
        } catch (e: Throwable) {
            throw e
        }
    }

    suspend fun removeBookmark(articleId: String) {
        articlePersonalDao.removeFromBookmark(articleId)
        if(preferences.accessToken.isEmpty()) {
            return
        }

        try {
            network.removeBookmark(articleId, preferences.accessToken)
        } catch (e: NoNetworkError) {
            return
        } catch (e: Throwable) {
            throw e
        }
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
