package ru.skillbranch.skillarticles.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*
import ru.skillbranch.skillarticles.R

import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.ui.custom.CommentItemView

class CommentsAdapter(private val listener: (CommentItemData) -> Unit) : PagedListAdapter<CommentItemData, CommentVH>(CommentDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentVH {
        LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        val containerView =  CommentItemView(parent.context) // LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentVH(containerView, listener)
    }

    override fun onBindViewHolder(holder: CommentVH, position: Int) {
        holder.bind(getItem(position))
    }
}

class CommentVH(override val containerView: View, val listener: (CommentItemData) -> Unit) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: CommentItemData?) {
        if (item != null) {
            itemView.setOnClickListener { listener(item) }
            //tv_author_name.text = item.user.name

        } else {
            // show placeholder - пока еще не загрузился из сети
            //tv_author_name.text = "Loading need placeholder"
        }

        (containerView as CommentItemView).bind(item)
    }
}

class CommentDiffCallback() : DiffUtil.ItemCallback<CommentItemData>() {
    override fun areItemsTheSame(oldItem: CommentItemData, newItem: CommentItemData): Boolean = ( oldItem.id == newItem.id)
    override fun areContentsTheSame(oldItem: CommentItemData, newItem: CommentItemData): Boolean = ( oldItem == newItem)
}