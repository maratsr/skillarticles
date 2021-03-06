package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId: String): BaseViewModel<ArticleState>(ArticleState()), IArticleViewModel {
    private val repository = ArticleRepository

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
    }

    // 3 метода трансформируеют observable в livedata
    // load text from network
    override  fun getArticleContent(): LiveData<List<Any>?> {
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
        //updateState { it.copy(isLike = !it.isLike) }
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
        updateState { it.copy(isSearch = isSearch)}
    }

    override fun handleSearch(query: String?) {
        updateState { it.copy(searchQuery = query) }
    }

    fun handleUpResult() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun handleDownResult() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    val searchResult: List<Pair<Int, Int>> = emptyList(), //результаты поиска( стартовая и конечные позиции)
    val searchPosition: Int = 0, // текущая позиция найденного результата
    val shareLink: String? = null, // ссылка Share
    val title: String? = null, // заголовок статьи
    val category: String? = null, // категория
    val categoryIcon: Any? = null, // иконка категории
    val date: String? = null, // дата публикации
    val author: Any? = null, // автор публикации
    val poster: Any? = null, // обложка статьи
    val content: List<Any> = emptyList(), // контент
    val reviews: List<Any> = emptyList() // комментарии
)