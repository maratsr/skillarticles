package ru.skillbranch.skillarticles.data.repositories

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
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

    private fun findArticlesByRange(start: Int, size: Int) = local.LOCAL_ARTICLE_ITEMS
        .drop(start) // Перенесемся на стартовую позицию
        .take(size) // Отдадим нужное число ArticleItemData

    private fun findBookmarkArticles(start: Int, size: Int) = local.LOCAL_ARTICLE_ITEMS
        .asSequence()
        .filter { it.isBookmark }
        .drop(start)
        .take(size)
        .toList()

    private fun searchBookmarkArticles(start: Int, size: Int,  query: String) = local.LOCAL_ARTICLE_ITEMS
        .asSequence()
        .filter { it.isBookmark  && it.title.contains(query, true)  }
        .drop(start)
        .take(size)
        .toList()

    private fun searchArticlesByTitle(start: Int, size: Int, queryTitle: String) =
        local.LOCAL_ARTICLE_ITEMS
            .asSequence()
            .filter { it.title.contains(queryTitle, true) }
            .drop(start)
            .take(size)
            .toList()

    fun loadArticlesFromNetwork(start: Int, size: Int): List<ArticleItem> =
        network.NETWORK_ARTICLE_ITEMS
            .drop(start)
            .take(size)
            .apply { sleep(500) } // Задержка для имитации получения по сети

    fun insertArticlesToDb(articles: List<ArticleItem>) {
        local.LOCAL_ARTICLE_ITEMS.addAll(articles)
            .apply { sleep(100) } // Задержка для имитации вставки в СУБД
    }

    fun updateBookmark(id: String, checked: Boolean) {
        val index = local.LOCAL_ARTICLE_ITEMS.indexOfFirst { it.id == id }
        if (index == -1) return
        local.LOCAL_ARTICLE_ITEMS[index] = local.LOCAL_ARTICLE_ITEMS[index].copy(isBookmark = checked)
    }


}

// Создание DataSource по соответствующей стратегии
class ArticlesDataFactory(val strategy: ArticleStrategy) :
    DataSource.Factory<Int, ArticleItem>() {
    override fun create(): DataSource<Int, ArticleItem> = ArticleDataSource(strategy)
}


class ArticleDataSource(private val strategy: ArticleStrategy) :
    PositionalDataSource<ArticleItem>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItem>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        callback.onResult(result, params.requestedStartPosition) // Передаем результат в callback
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItem>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        callback.onResult(result)
    }
}

// Разные источники данных
sealed class ArticleStrategy() {
    abstract fun getItems(start: Int, size: Int): List<ArticleItem>

    class AllArticles( // Загрузить все статьи
        private val itemProvider: (Int, Int) -> List<ArticleItem>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> =
            itemProvider(start, size)
    }

    class SearchArticle( // Загрузить статьи содержащие строку поиска
        private val itemProvider: (Int, Int, String) -> List<ArticleItem>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> =
            itemProvider(start, size, query)
    }

    class SearchBookmark(
        private val itemProvider: (Int, Int, String) -> List<ArticleItem>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> =
            itemProvider(start, size, query)
    }

    class BookmarkArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItem>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> =
            itemProvider(start, size)
    }
}

class ArticleFilter(
    val search: String? = null,
    val isBookmark: Boolean = false,
    val categories: List<String> = listOf(),
    val isHashtag:Boolean = false
) {
    fun toQuery(): String{
        val qb= QueryBuilder()
        qb.table("ArticleItem")

        if (search != null && !isHashtag) qb.appendWhere("title LIKE '%$search%'")
        if (search != null && isHashtag) {
            qb.innerJoin("article_tag_x_ref as refs", "refs.a_id=id")
            qb.appendWhere("refs.t_id =$search%")
        }

        if (isBookmark) qb.appendWhere("is_bookmark = 1")
        if (categories.isNotEmpty()) qb.appendWhere("category_id IN (${categories.joinToString(",")})")

        qb.orderBy("date")
        return qb.build()

    }
}

class QueryBuilder(){
    private var table:String? = null
    private var selectColumns: String = "*"
    private var joinTables: String? = null
    private var whereCondition: String? = null
    private var order: String? = null

    fun build(): String {
        check(table!=null) {"table must not be null"}
        val strBuilder = StringBuilder("SELECT ")
            .append("$selectColumns ")
            .append("FROM $table ")

        if(whereCondition != null) strBuilder.append(whereCondition)
        if (order!= null) strBuilder.append(order)
        return strBuilder.toString()
    }

    fun table(table: String): QueryBuilder {
        this.table= table
        return this
    }

    fun orderBy(column: String, isDesc: Boolean = true): QueryBuilder {
        order = "ORDER BY $column ${if(isDesc) "DESC" else "ASC"}"
        return this
    }

    fun appendWhere(condition: String, logic: String="AND") : QueryBuilder {
        if(whereCondition.isNullOrEmpty()) whereCondition = "WHERE $condition "
        else whereCondition += "$logic $condition "
        return this
    }

    fun innerJoin(table: String, on: String): QueryBuilder {
        if(joinTables.isNullOrEmpty()) joinTables = "INNER JOIN $table ON $on "
        else joinTables += "INNER JOIN $table ON $on "
        return this
    }

}
