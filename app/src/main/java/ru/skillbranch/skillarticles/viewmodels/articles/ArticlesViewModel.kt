package ru.skillbranch.skillarticles.viewmodels.articles

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleStrategy
import ru.skillbranch.skillarticles.data.repositories.ArticlesDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ArticlesViewModel(handle: SavedStateHandle) : BaseViewModel<ArticlesState>(handle, ArticlesState()) {
    private val repository = ArticlesRepository

    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(false) // Нет placeholder-ов для списка статей
            .setPageSize(10) // Число страниц в chank-е
            .setPrefetchDistance(30)
            .setInitialLoadSizeHint(50) // Начальное кол-во загружаемых данных
            .build()
    }

    private val listData: LiveData<PagedList<ArticleItemData>> =
        Transformations.switchMap(state) {// подписываемся на state и в зависимости от state будем переключать datasource
            if (it.isSearch && !it.searchQuery.isNullOrBlank()) buildPagedList(repository.searchArticles(it.searchQuery))
            else buildPagedList(repository.allArticles())
        }  // buildPagedList(repository.allArticles())



//    private val listData: LiveData<PagedList<ArticleItemData>> = Transformations.switchMap(state) {
//        if (it.isSearch && !it.searchQuery.isNullOrBlank()) buildPagedList(repository.searchArticles(it.searchQuery))
//        else buildPagedList(repository.allArticles())
//    }

//    init {
//        subscribeOnDataSource(repository.loadArticles()) { articles, state ->
//            articles ?: return@subscribeOnDataSource null
//            state.copy(articles=articles)
//        }
//    }

    fun observeList(owner: LifecycleOwner, onChange: (list: PagedList<ArticleItemData>) -> Unit) {
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPagedList(dataFactory: ArticlesDataFactory): LiveData<PagedList<ArticleItemData>> {
        val builder = LivePagedListBuilder<Int, ArticleItemData>(dataFactory, listConfig)

        // если режим = все статьи
        if (dataFactory.strategy is ArticleStrategy.AllArticles) {
            builder.setBoundaryCallback(ArticlesBoundaryCallback(::zeroLoadingHandle, ::itemAtEndHandle))
        }
        return builder.setFetchExecutor(Executors.newSingleThreadExecutor()).build() // Подготовка данных будет на новом thread

    }

    private fun zeroLoadingHandle() { // Если данные закончились
        Log.e("ArticlesViewModel","zeroLoadingHandle")
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            // загружаем из сети
            val items = repository.loadArticlesFromNetwork(0, listConfig.initialLoadSizeHint)
            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items) // Закинем в БД
                listData.value?.dataSource?.invalidate() // создасm новый LiveData<PagedList>
            }
        }
    }

    private fun itemAtEndHandle(itemAtEnd: ArticleItemData) { // доскроллили до конца
        Log.e("ArticlesViewModel","itemAtEndHandle")
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(itemAtEnd.id.toInt() + 1, listConfig.pageSize)

            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }

            withContext(Dispatchers.Main) {
                notify(Notify.TextMessage("Loaded from network from ${items.firstOrNull()?.id} to ${items.lastOrNull()?.id}"))
            }
        }
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState { it.copy(searchQuery = query)}
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch)}
    }

    fun handleToggleBookmark(id: String, isChecked: Boolean) {
        repository.updateBookmark(id, isChecked)
        listData.value?.dataSource?.invalidate()
    }
}

data class ArticlesState(
    val isSearch: Boolean = false, val searchQuery: String? = null, val isLoading: Boolean = true): IViewModelState {
    override fun save(outState: SavedStateHandle) {
        outState.set(::isSearch.name, isSearch)
        outState.set(::searchQuery.name, searchQuery)
        outState.set(::isLoading.name, isLoading)
    }

    override fun restore(savedState: SavedStateHandle): IViewModelState = copy(
        isSearch = savedState.get<Boolean>(::isSearch.name) ?: false,
        searchQuery = savedState.get<String>(::searchQuery.name),
        isLoading = savedState.get<Boolean>(::isLoading.name) ?: true)
}


// Класс для задания callback событий, передавая их при создании
class ArticlesBoundaryCallback(private val zeroLoadingHandle: () -> Unit, private val itemAtEndHandle: (itemAtEnd: ArticleItemData) -> Unit):
    PagedList.BoundaryCallback<ArticleItemData>() {

    override fun onZeroItemsLoaded() = zeroLoadingHandle() // Callback если данные закончились
    override fun onItemAtEndLoaded(itemAtEnd: ArticleItemData) =  itemAtEndHandle(itemAtEnd) // Callback - если доскроллили до низа
}