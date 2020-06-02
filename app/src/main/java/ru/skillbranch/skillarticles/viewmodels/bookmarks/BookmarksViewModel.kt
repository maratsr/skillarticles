package ru.skillbranch.skillarticles.viewmodels.bookmarks

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticlesDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import java.util.concurrent.Executors

class BookmarksViewModel(handle: SavedStateHandle) : BaseViewModel<BookmarksState>(handle, BookmarksState()) {
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
            if (it.isSearch && !it.searchQuery.isNullOrBlank()) buildPagedList(repository.searchBookmarksArticles(it.searchQuery))
            else buildPagedList(repository.getBookmarksArticles())
        }  // buildPagedList(repository.allArticles())

    private fun buildPagedList(dataFactory: ArticlesDataFactory): LiveData<PagedList<ArticleItemData>> {
        return LivePagedListBuilder(dataFactory, listConfig)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    fun observeList(owner: LifecycleOwner, onChange: (list: PagedList<ArticleItemData>) -> Unit) {
        listData.observe(owner, Observer { onChange(it) })
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

data class BookmarksState(
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
