package ru.skillbranch.skillarticles.ui.articles

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_articles.*

import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesState
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class ArticlesFragment : BaseFragment<ArticlesViewModel>() {
    override val viewModel: ArticlesViewModel by viewModels()
    override val layout = R.layout.fragment_articles
    override val binding: ArticlesBinding by lazy {ArticlesBinding()}

    private val articlesAdapter = ArticlesAdapter {item ->
        Log.d("ArticlesFragment", "click on article ${item.id}")
        // данные для перемещения на фрагмент
        val action = ArticlesFragmentDirections.actionNavArticlesToPageArticle(item.id, item.author,
            item.authorAvatar, item.category, item.categoryIcon, item.date, item.poster, item.title)
        viewModel.navigate(NavigationCommand.To(action.actionId, action.arguments))

    }

    override fun setupViews() {
        with(rv_articles) {
            layoutManager = LinearLayoutManager(context)
            adapter = articlesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        // Подписка на список
        viewModel.observeList(viewLifecycleOwner) { data -> articlesAdapter.submitList(data)}
    }

    inner class ArticlesBinding : Binding() {
        var isFocusedSearch = false
        var searchQuery: String? = null
        var isSearch = false
        var isLoading: Boolean by RenderProp(true) {
            // TODO: Show shimmer on rv_list
        }

        override fun bind(data: IViewModelState) {
            data as ArticlesState
            isSearch = data.isSearch
            searchQuery = data.searchQuery
            isLoading = data.isLoading
        }

        // TODO: save ui
    }
}
