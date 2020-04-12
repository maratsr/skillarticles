package ru.skillbranch.skillarticles.ui

import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify


class RootActivity : BaseActivity<RootViewModel>() {

//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    public override val binding: ArticleBinding by lazy { ArticleBinding() }

    override val layout: Int = R.layout.activity_root
    override val viewModel: RootViewModel by viewModels ()

//
//    override fun setupViews() {
//        setupToolbar()
//        setupBottombar()
//        setupSubmenu()
//    }
//
//    override fun showSearchBar() {
//        bottombar.setSearchState(true)
//        // Необходимо добавить отступ снизу чтобы мы могли пролистать до низа весь контент (не перекрывать его панелью поиска)
//        scroll.setMarginOptionally(bottom=dpToIntPx(56))
//    }
//
//    override fun hideSearchBar() {
//        bottombar.setSearchState(false)
//        // Убрать отступ снизу
//        scroll.setMarginOptionally(bottom=dpToIntPx(0))
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_search, menu)
//        val menuItem = menu?.findItem(R.id.action_search)
//        val searchView = menuItem?.actionView as? SearchView
//
//        if (binding.isSearch) {
//            menuItem?.expandActionView() // Если был режим поиска - восстановим
//            searchView?.setQuery(binding.searchQuery, false)
//
//            if(binding.isFocusedSearch) searchView?.requestFocus()
//            else searchView?.clearFocus()
//        }
//
//        // https://stackoverflow.com/questions/55537368/filter-for-searchview-in-kotlin
//        with(menu!!.findItem(R.id.action_search)) {
//            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
//                    viewModel.handleSearchMode(true)
//                    return true
//                }
//
//                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
//                    viewModel.handleSearchMode(false)
//                    return true
//                }
//            })
//        }
//
//        with( menu.findItem(R.id.action_search).actionView as SearchView) {
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String?): Boolean {
//                    viewModel.handleSearch(query)
//                    return true
//                }
//
//                override fun onQueryTextChange(newText: String?): Boolean {
//                    viewModel.handleSearch(newText)
//                    return true
//                }
//            })
//        }
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar
            .make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
//            .setAnchorView(bottombar)


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

    override fun subscribeOnState(state: IViewModelState) {

    }

}
