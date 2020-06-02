package ru.skillbranch.skillarticles.ui.articles

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.data.models.ArticleItemData

import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class ArticlesAdapter(
    private val articleListener: (ArticleItemData) -> Unit,
    private val bookmarkListener: (String, Boolean) -> Unit) : PagedListAdapter<ArticleItemData, ArticleVH>(ArticleDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val containerView = ArticleItemView(parent.context) //LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleVH(containerView, bookmarkListener)
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), articleListener )
    }
}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItemData>() {
    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean  =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem == newItem
}

class ArticleVH(private val containerView: View, private val bookmarkListener: (String, Boolean) -> Unit): RecyclerView.ViewHolder(containerView) {
    fun bind(item: ArticleItemData?, listener: (ArticleItemData) -> Unit) {

        // При использовании placeholders проверка на item is null
        item?.let { it ->
            (containerView as ArticleItemView).bind(it, bookmarkListener) // Также передаем listener клика bookmark-а
            itemView.setOnClickListener { listener(item) }
        }
    }
}
