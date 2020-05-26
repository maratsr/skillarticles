package ru.skillbranch.skillarticles.viewmodels.articles

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
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

    private val listData = buildPagedList(repository.allArticles())


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

    private fun buildPagedList(dataFactory: DataSource.Factory<Int, ArticleItemData>): LiveData<PagedList<ArticleItemData>> {
        val builder = LivePagedListBuilder<Int, ArticleItemData>(dataFactory, listConfig)
        return builder.setFetchExecutor(Executors.newSingleThreadExecutor()).build() // Подготовка данных будет на новом thread

    }

//    private fun buildPagedList(dataFactory: ArticlesDataFactory): LiveData<PagedList<ArticleItemData>> {
//        val builder = LivePagedListBuilder<Int, ArticleItemData>(dataFactory, listConfig)
//
//        if (dataFactory.strategy is ArticleStrategy.AllArticles) {
//            builder.setBoundaryCallback(ArticlesBoundaryCallback(::zeroLoadingHandle, ::itemAtEndHandle))
//        }
//
//        return builder.setFetchExecutor(Executors.newSingleThreadExecutor()).build()
//    }
}

data class ArticlesState(
    val isSearch: Boolean = false, val searchQuery: String? = null, val isLoading: Boolean = true): IViewModelState

class ArticlesBoundaryCallback(private val zeroLoadingHandle: () -> Unit, private val itemAtEndHandle: (itemAtEnd: ArticleItemData) -> Unit):
    PagedList.BoundaryCallback<ArticleItemData>() {

    override fun onZeroItemsLoaded() = zeroLoadingHandle()
    override fun onItemAtEndLoaded(itemAtEnd: ArticleItemData) =  itemAtEndHandle(itemAtEnd)
}