package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.getSpans
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.ObserveProp
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelDelegate


class RootActivity : BaseActivity<ArticleViewModel>(), IArticleView {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override val binding: ArticleBinding by lazy { ArticleBinding() }

    override val layout: Int = R.layout.activity_root
//    override val viewModel: ArticleViewModel by lazy {
//        val vmFactory = ViewModelFactory("0")
//        ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
//    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override val viewModel: ArticleViewModel by provideViewModel("0")

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val bgColor by AttrValue(R.attr.colorSecondary) // ранее было  Color.RED

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val fgColor by AttrValue(R.attr.colorOnSecondary) // = Color.WHITE

    override fun setupViews() {
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        Log.d("RenderSearchResult", "searchResult $searchResult")
        val content = tv_text_content.text as Spannable


        clearSearchResult()

        // int, int  - начало и конец вхождения
        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(bgColor, fgColor),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Переведем скролл на самое первое вхождение
        renderSearchPosition(0)
    }

    override fun renderSearchPosition(searchPosition: Int) {
        // Победить -1 searchPosition и выще индекса списка
        Log.d("SearchPosition", "search position=$searchPosition")
        val content = tv_text_content.text as Spannable
        val spans = content.getSpans<SearchSpan>()

        // очистим контент на котором фокус (lastsearchposition)
        content.getSpans<SearchFocusSpan>().forEach {
            content.removeSpan(it)
        }

        if (spans.isNotEmpty()) {
            //find position span
            val result = spans[searchPosition]
            // Перемещаем выделение чтобы делать перемещения
            Selection.setSelection(content, content.getSpanStart(result))
            // И установить выделение при поиске
            content.setSpan(
                SearchFocusSpan(bgColor, fgColor),
                content.getSpanStart(result),
                content.getSpanEnd(result),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    // Очистить предыдущие результаты поиска
    override fun clearSearchResult() {
        val content = tv_text_content.text as Spannable
        content.getSpans<SearchSpan>() // Для всех найденных спанов SearchSpan типа
            .forEach { content.removeSpan(it) } // Чистим результаты
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        // Необходимо добавить отступ снизу чтобы мы могли пролистать до низа весь контент (не перекрывать его панелью поиска)
        scroll.setMarginOptionally(bottom=dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        // Убрать отступ снизу
        scroll.setMarginOptionally(bottom=dpToIntPx(0))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as? SearchView

        if (binding.isSearch) {
            menuItem?.expandActionView() // Если был режим поиска - восстановим
            searchView?.setQuery(binding.searchQuery, false)

            if(binding.isFocusedSearch) searchView?.requestFocus()
            else searchView?.clearFocus()
        }

        // https://stackoverflow.com/questions/55537368/filter-for-searchview-in-kotlin
        with(menu!!.findItem(R.id.action_search)) {
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    viewModel.handleSearchMode(true)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.handleSearchMode(false)
                    return true
                }
            })
        }

        with( menu.findItem(R.id.action_search).actionView as SearchView) {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.handleSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.handleSearch(newText)
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar
            .make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)


        when (notify) { // переберем варианты представителей sealed class-а
            is Notify.TextMessage -> {
                /* nothing */
            }

            is Notify.ActionMessage -> { // пример: при like статьи переспросить в snackbar-е с хендлом смены состояния на лайк
                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(notify.actionLabel) {
                        notify.actionHandler.invoke()
                    }
                }
            }

            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) { notify.errHandler?.invoke() }
                }
            }
        }

        snackbar.show()
    }

    // Установим наши listener-ы action-ы
    private fun setupSubmenu() {
        btn_text_up.setOnClickListener{ viewModel.handleUpText()}
        btn_text_down.setOnClickListener{ viewModel.handleDownText()}
        switch_mode.setOnClickListener{ viewModel.handleNightMode()}
    }

    // Установим наши listener-ы action-ы,
    // при вызове action - изменяется состояние у ViewModel ->
    // это измененное состояние получаем в observState (в качестве аргумента лямбды) ->
    // вызываем renderUi, куда передаем изменный State
    private fun setupBottombar() {
        btn_like.setOnClickListener{viewModel.handleLike()}
        btn_bookmark.setOnClickListener{viewModel.handleBookmark()}
        btn_share.setOnClickListener{viewModel.handleShare()}
        btn_settings.setOnClickListener{viewModel.handleToggleMenu()}

        btn_result_up.setOnClickListener {
            if(search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleUpResult()
        }

        btn_result_down.setOnClickListener {
            if(search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleDownResult()
        }

        btn_search_close.setOnClickListener {
            viewModel.handleSearchMode(false)
            invalidateOptionsMenu() // toolbar - в изначальное состоние
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // НАйдем logo и будем работать с ним как с ImageView вручную (так как через разметку кастомизируется не все)
        val logo = if (toolbar.childCount>2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams

        lp?.let{
            it.width= this.dpToIntPx(40) // Зададим высоту и ширину лого
            it.height= this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16) // Зададим отступ справа
            logo.layoutParams = it
        }
    }

    inner class ArticleBinding(): Binding() {
        var isFocusedSearch: Boolean = false  // режим поиска
        var searchQuery: String? = null // строка поиска
        private var isLoadingContent by ObserveProp(true)

        private var isLike: Boolean by RenderProp(false ) {btn_like.isChecked = it}
        private var isBookmark: Boolean by RenderProp(false ) {btn_bookmark.isChecked = it}
        private var isShowMenu: Boolean by RenderProp(false ) {
            btn_settings.isChecked = it
            if(it) submenu.open() else submenu.close()
        }
        private var title: String by RenderProp ("loading") {toolbar.title = it }
        private var category: String by RenderProp ("loading") {toolbar.subtitle = it }
        private var categoryIcon: Int by RenderProp (R.drawable.logo_placeholder) {toolbar.logo =getDrawable(it) }
        private var isBigText: Boolean by RenderProp(false) {
            if(it) {
                tv_text_content.textSize = 18f
                btn_text_up.isChecked = true
                btn_text_down.isChecked = false
            } else {
                tv_text_content.textSize = 14f
                btn_text_up.isChecked = false
                btn_text_down.isChecked = true

            }
        }
        // Инифициализацию отключили чтобы не зациклить day-night-day-...
        private var isDarkMode: Boolean by RenderProp(false, false ) {
            switch_mode.isChecked = it
            delegate.localNightMode = if (it) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        }

        var isSearch: Boolean by ObserveProp(false) {
            if (it) showSearchBar() else hideSearchBar()
        }

        private var searchResults: List<Pair<Int, Int>> by ObserveProp(emptyList())
        private var searchPosition: Int by ObserveProp(0)
        private var content: String by ObserveProp("loading") {
            tv_text_content.setText(it, TextView.BufferType.SPANNABLE)
            tv_text_content.movementMethod = ScrollingMovementMethod()
        }

        override fun onFinishInflate() {
            // Наблюдаемые поля (поэтому ::)
            dependsOn<Boolean, Boolean, List<Pair<Int,Int>>, Int>(
                ::isLoadingContent, ::isSearch, ::searchResults, ::searchPosition
            ){ ilc, iss, sr, sp ->
                if(!ilc && iss) {
                    renderSearchResult(sr)
                    renderSearchPosition(sp)
                }
                if(!ilc && !iss) { // Очистим результаты поиска после выхода
                    clearSearchResult()
                }

                bottombar.bindSearchInfo(sr.size, sp)
            }
        }

        override fun bind(data: IViewModelState) {
            data as ArticleState
            isLike = data.isLike
            isBookmark = data.isBookmark
            isShowMenu = data.isShowMenu
            isBigText = data.isBigText
            isDarkMode = data.isDarkMode

            if(data.title != null) title = data.title
            if(data.category != null) category = data.category
            if(data.categoryIcon != null) categoryIcon = data.categoryIcon as Int
            if(data.content.isNotEmpty()) content = data.content.first() as String

            isLoadingContent = data.isLoadingContent
            isSearch = data.isSearch
            searchQuery = data.searchQuery
            searchPosition = data.searchPosition
            searchResults = data.searchResults

        }

        override fun saveUi(outState: Bundle) {
            outState.putBoolean(::isFocusedSearch.name, search_view?.hasFocus() ?: false)
        }

        override fun restoreUi(savedState: Bundle) {
            isFocusedSearch = savedState.getBoolean(::isFocusedSearch.name)
        }
    }
}
