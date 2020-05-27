package ru.skillbranch.skillarticles.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.*
import ru.skillbranch.skillarticles.data.models.*
import kotlin.math.abs

// Singleton репозиторий - эмуляция получения данных
object ArticleRepository {
    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun loadArticleContent(articleId: String): LiveData<List<MarkdownElement>?> {
        //5s delay from network
        return Transformations.map(network.loadArticleContent(articleId)) {
            return@map if (it == null) null
            else MarkdownParser.parse(it)
        }
    }

    fun getArticle(articleId: String): LiveData<ArticleData?> {
        return local.findArticle(articleId) //2s delay from db
    }

    fun loadArticlePersonalInfo(articleId: String): LiveData<ArticlePersonalInfo?> {
        return local.findArticlePersonalInfo(articleId) //1s delay from db
    }

    fun getAppSettings(): LiveData<AppSettings> = local.getAppSettings() //from preferences
    fun updateSettings(appSettings: AppSettings) {
        local.updateAppSettings(appSettings)
    }

    fun updateArticlePersonalInfo(info: ArticlePersonalInfo) {
        local.updateArticlePersonalInfo(info)
    }

    fun isAuth(): MutableLiveData<Boolean> = local.isAuth()

    fun allComments(articleId: String, totalCount: Int): CommentsDataFactory =
        CommentsDataFactory(::loadCommentsByRange, articleId, totalCount)

    private fun loadCommentsByRange(slug: String?, size: Int, articleId: String): List<CommentItemData> {
        val data = network.commentsData.getOrElse(articleId) { mutableListOf() }
        return when {
            slug == null -> data.take(size) // начальная загрузка
            size > 0 -> data.dropWhile { it.slug != slug }.drop(1).take(size)
            size < 0 -> data.dropLastWhile { it.slug != slug }.dropLast(1).takeLast(abs(size)) // отбросим с конца
            else -> emptyList()
        }
    }

    fun sendComment(articleId: String, comment: String, answerToSlug: String?) {
        network.sendMessage(articleId, comment, answerToSlug, User("777", "John Doe", "https://miro.medium.com/fit/c/96/96/0*zhOjC9mtKiAzmBQo.png"))
        local.incrementCommentsCount(articleId)
    }

}

// Комментарии к статье
class CommentsDataFactory(
    private val itemProvider: (String?, Int, String) -> List<CommentItemData>,
    private val articleId: String,
    private val totalCount: Int // кол-во комментариев
) : DataSource.Factory<String?, CommentItemData>() {
    override fun create(): DataSource<String?, CommentItemData> {
        return CommentsDataSource(itemProvider, articleId, totalCount)
    }
}

class CommentsDataSource( // необходимо заимплементить 4 метода
    private val itemProvider: (String?, Int, String) -> List<CommentItemData>,
    private val articleId: String,
    private val totalCount: Int
): ItemKeyedDataSource<String, CommentItemData>() {
    // Загрузка первоначальных значений
    override fun loadInitial( params: LoadInitialParams<String>, callback: LoadInitialCallback<CommentItemData>) {
        val result = itemProvider(params.requestedInitialKey, params.requestedLoadSize, articleId)
        Log.e("ArticleRepository", "loadInitial key> ${params.requestedInitialKey} size> ${result.size} total> $totalCount")
        callback.onResult(if (totalCount > 0) result else emptyList(), 0, totalCount)
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<CommentItemData>) {
        val result = itemProvider(params.key, params.requestedLoadSize, articleId)
        Log.e("ArticleRepository", "loadAfter key> ${params.key} size> ${result.size}")
        callback.onResult(result)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<CommentItemData>) {
        val result = itemProvider(params.key, -params.requestedLoadSize, articleId)
        Log.e("ArticleRepository", "loadBefore key> ${params.key} size> ${result.size}")
        callback.onResult(result)
    }

    override fun getKey(item: CommentItemData): String = item.slug
}