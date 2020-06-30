package ru.skillbranch.skillarticles.data.repositories

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import java.lang.Thread.sleep

object ArticlesRepository {

    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun allArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticleStrategy.AllArticles(::findArticlesByRange))

    fun searchArticles(searchQuery: String) =
        ArticlesDataFactory(ArticleStrategy.SearchArticle(::searchArticlesByTitle, searchQuery))

    fun allBookmarked(): ArticlesDataFactory =
        ArticlesDataFactory(ArticleStrategy.BookmarkArticles(::findBookmarkArticles))

    fun searchBookmarkedArticles(searchQuery: String): ArticlesDataFactory =
        ArticlesDataFactory(ArticleStrategy.SearchBookmark(::searchBookmarkArticles, searchQuery))

    private fun findArticlesByRange(start: Int, size: Int) = local.localArticleItems
        .drop(start) // Перенесемся на стартовую позицию
        .take(size) // Отдадим нужное число ArticleItemData

    private fun findBookmarkArticles(start: Int, size: Int) = local.localArticleItems
        .asSequence()
        .filter { it.isBookmark }
        .drop(start)
        .take(size)
        .toList()

    private fun searchBookmarkArticles(start: Int, size: Int,  query: String) = local.localArticleItems
        .asSequence()
        .filter { it.isBookmark  && it.title.contains(query, true)  }
        .drop(start)
        .take(size)
        .toList()

    private fun searchArticlesByTitle(start: Int, size: Int, queryTitle: String) =
        local.localArticleItems
            .asSequence()
            .filter { it.title.contains(queryTitle, true) }
            .drop(start)
            .take(size)
            .toList()

    fun loadArticlesFromNetwork(start: Int, size: Int): List<ArticleItemData> =
        network.networkArticleItems
            .drop(start)
            .take(size)
            .apply { sleep(500) } // Задержка для имитации получения по сети

    fun insertArticlesToDb(articles: List<ArticleItemData>) {
        local.localArticleItems.addAll(articles)
            .apply { sleep(100) } // Задержка для имитации вставки в СУБД
    }

    fun updateBookmark(id: String, checked: Boolean) {
        val index = local.localArticleItems.indexOfFirst { it.id == id }
        if (index == -1) return
        local.localArticleItems[index] = local.localArticleItems[index].copy(isBookmark = checked)
    }


}

// Создание DataSource по соответствующей стратегии
class ArticlesDataFactory(val strategy: ArticleStrategy) :
    DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> = ArticleDataSource(strategy)
}


class ArticleDataSource(private val strategy: ArticleStrategy) :
    PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItemData>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        callback.onResult(result, params.requestedStartPosition) // Передаем результат в callback
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        callback.onResult(result)
    }
}

// Разные источники данных
sealed class ArticleStrategy() {
    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles( // Загрузить все статьи
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)
    }

    class SearchArticle( // Загрузить статьи содержащие строку поиска
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)
    }

    class SearchBookmark(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)
    }

    class BookmarkArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)
    }
}