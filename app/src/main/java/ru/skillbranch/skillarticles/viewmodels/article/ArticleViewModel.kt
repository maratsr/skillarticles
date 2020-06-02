package ru.skillbranch.skillarticles.viewmodels.article

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleData
import ru.skillbranch.skillarticles.data.models.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.CommentsDataFactory
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticleViewModel(
    handle: SavedStateHandle,
    private val articleId: String): BaseViewModel<ArticleState>(handle, ArticleState()), IArticleViewModel {
    private val repository = ArticleRepository
    private var clearContent: String? = null

    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5) // За один раз грузим по 5 комментариев
            .build()
    }

    private val listData: LiveData<PagedList<CommentItemData>> = Transformations.switchMap(getArticleData()) {
        buildPagedList(repository.allComments(articleId, it?.commentCount ?: 0))
    }

    init {
        subscribeOnDataSource(getArticleData()) { article, state -> // получение из БД
            article ?: return@subscribeOnDataSource null// если null - выходим из метода
            state.copy( // меняем значения на пришедшие из источника
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                author = article.author,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, state -> // получение из сети
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) {info, state -> // персональная инфо о статье (понравилась...) - из СУБД
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText

            )
        }

        subscribeOnDataSource(repository.isAuth()) { auth, state ->
            state.copy(isAuth = auth)

        }
    }

    // 3 метода трансформируеют observable в livedata
    // load text from network
    override  fun getArticleContent(): LiveData<List<MarkdownElement>?> {
        return repository.loadArticleContent(articleId)
    }

    //load data from db
    override fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    //load data from db
    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        // При этом MediatorLiveData узнает об изменении состояния -> изменит общ состояния и UI
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleLike() {
        val toggleLike = { // Функция обработки
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val msg =
            if (currentState.isLike) Notify.TextMessage("Mark is liked")
            else Notify.ActionMessage( // доп запрос на смену состояния
                "Don`t like it anymore", // message
                "No, still like it", // label
                toggleLike // handle
            )

        notify(msg)
    }

    override fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))

        notify( Notify.TextMessage(
            if (!info.isBookmark) "Add to bookmarks" else "Remove from bookmarks"))
    }

    override fun handleShare() { // Обработка нажатия на share
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))

    }

    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) } // используем copy - меняя значение
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0)}
    }

    override fun handleSearch(query: String?) {
        query ?: return
        if (clearContent == null && currentState.content.isNotEmpty()) clearContent = currentState.content.clearContent()
        val result = clearContent
            .indexesOf(query)
            .map{it to it + query.length}
        updateState { it.copy(searchQuery = query, searchResults = result, searchPosition = 0) } // обновил
    }

    fun handleUpResult() {
        updateState{
            it.copy(searchPosition = it.searchPosition.dec())
        }
    }

    fun handleDownResult() {
        updateState{
            it.copy(searchPosition = it.searchPosition.inc())}

    }

    fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }

    fun handleSendComment(comment: String) { // Перекинем на страницу авторизации
        if (!currentState.isAuth) navigate(NavigationCommand.StartLogin())
       updateState { it.copy(comment = comment) }

        if (comment.isNullOrBlank()) {
            notify(Notify.TextMessage("Comment must not be empty"))
            return
        }

        if (!currentState.isAuth) {
            navigate(NavigationCommand.StartLogin())
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.sendComment(articleId, comment, currentState.answerToSlug)
            withContext(Dispatchers.Main) {
                updateState { it.copy(answerTo = null, answerToSlug = null, comment = null) }
                //updateState { it.copy(answerTo = null, answerToSlug = null) }
            }
        }
    }

    fun observeList(owner: LifecycleOwner, onChanged: (list: PagedList<CommentItemData>) -> Unit) {
        listData.observe(owner, Observer { onChanged(it) })
    }

    private fun buildPagedList(dataFactory: CommentsDataFactory): LiveData<PagedList<CommentItemData>> {
        return LivePagedListBuilder<String, CommentItemData>(dataFactory, listConfig)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(showBottomBar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(answerTo = null, answerToSlug = null) }
    }

    fun handleReplyTo(slug: String, name: String) {
        updateState { it.copy(answerToSlug = slug, answerTo = "Reply to $name") }
    }

}

data class ArticleState(
    val isAuth: Boolean = false, // Пользователь авторизован
    val isLoadingContent: Boolean = true, // контент загружается
    val isLoadingReview: Boolean = true, // отзывы загружаются
    val isLike: Boolean = false, // отмечено как Like
    val isBookmark: Boolean = false, // в закладках
    val isShowMenu: Boolean = false, // отображается меню
    val isBigText: Boolean = false, // шрифт увеличен
    val isDarkMode: Boolean = false, // темный режим
    val isSearch: Boolean = false, // режим поиска
    val searchQuery: String? = null, // поисковый запрос
    val searchResults: List<Pair<Int, Int>> = emptyList(), //результаты поиска( стартовая и конечные позиции)
    val searchPosition: Int = 0, // текущая позиция найденного результата
    val shareLink: String? = null, // ссылка Share
    val title: String? = null, // заголовок статьи
    val category: String? = null, // категория
    val categoryIcon: Any? = null, // иконка категории
    val date: String? = null, // дата публикации
    val author: Any? = null, // автор публикации
    val poster: Any? = null, // обложка статьи
    val content: List<MarkdownElement> = emptyList(), // контент
    val commentCount: Int=0, // комментарии
    val answerTo: String? = null,
    val answerToSlug: String? = null,
    val showBottomBar: Boolean = true,
    val comment: String? = null
) : IViewModelState {
    override fun save(outState: SavedStateHandle) { //Сохраняем как ключ, значение
                outState.set("isSearch", isSearch)
                outState.set("searchQuery", searchQuery)
                outState.set("searchResults", searchResults)
                outState.set("searchPosition", searchPosition)
    }

    override fun restore(savedState: SavedStateHandle): ArticleState {
        return copy(
            isSearch = savedState["isSearch"] ?: false,
            searchQuery = savedState["searchQuery"],
            searchResults = savedState["searchResults"] ?: emptyList(),
            searchPosition = savedState["searchPosition"] ?: 0)
    }
}