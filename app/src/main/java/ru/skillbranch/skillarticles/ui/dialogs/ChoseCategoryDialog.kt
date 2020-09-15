package ru.skillbranch.skillarticles.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_category.view.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData

class ChoseCategoryDialog : DialogFragment() {
    //private val viewModel : ArticlesViewModel by activityViewModels() // shared viewModel для 3х фрагментов
    private val selected = mutableListOf<String>()
    private val args : ChoseCategoryDialogArgs by  navArgs()

    companion object {
        const val CHOOSE_CATEGORY_KEY = "CHOOSE_CATEGORY_KEY"
        const val SELECTED_CATEGORIES = "SELECTED_CATEGORIES"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val categories = args.categories.toList()  // Собственно элементы

        selected.addAll(
            savedInstanceState
                ?.getStringArray(::selected.name) ?:
            args.selectedCategories
        )

        val categoriesListAdapter =
            CategoryAdapter() { item, isChecked ->
                if (isChecked)
                    selected.add(item.categoryId)
                else
                    selected.remove(item.categoryId)
            }.apply {
                submitList(categories.map {
                    it.toCategoryItem(isChecked = it.categoryId in selected)
                })
            }

        val adb = AlertDialog.Builder(requireContext())
            .setTitle("Chose categories")
            .setView((layoutInflater.inflate(R.layout.fragment_categories_dialog, null) as RecyclerView)
                .apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = categoriesListAdapter
                })
            .setPositiveButton("Apply") {_, _ ->
                setFragmentResult(CHOOSE_CATEGORY_KEY, bundleOf(SELECTED_CATEGORIES to selected.toList()))
            }
            .setNegativeButton("Reset") {_, _ ->
                setFragmentResult(CHOOSE_CATEGORY_KEY, bundleOf(SELECTED_CATEGORIES to emptyList<String>())) }
        return adb.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArray(::selected.name, selected.toTypedArray())
    }

    private class CategoryAdapter(
        private val listener: (CategoryItem, Boolean) -> Unit
    ) : ListAdapter<CategoryItem, RecyclerView.ViewHolder>(CategoriesDiffUtilCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return CategoriesVH(view, listener)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = getItem(position)
            (holder as CategoriesVH).bind(item)
        }
    }

    private data class CategoryItem(
        val categoryId: String,
        val icon: String,
        val title: String,
        val articlesCount: Int = 0,
        val isChecked: Boolean = false
    )

    private fun CategoryData.toCategoryItem(isChecked: Boolean) = CategoryItem(
        categoryId = categoryId,
        icon = icon,
        title = title,
        articlesCount = articlesCount,
        isChecked = isChecked
    )

    private class CategoriesVH(
        val containerView: View,
        val listener: (CategoryItem, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(containerView) {

        fun bind(item: CategoryItem) {
            with(containerView) {
                Glide.with(containerView)
                    .load(item.icon)
                    .into(iv_icon)

                ch_select.isChecked = item.isChecked
                tv_category.text = item.title
                tv_count.text = item.articlesCount.toString()

                ch_select.setOnCheckedChangeListener { _, isChecked ->  listener(item, isChecked) }
                setOnClickListener { ch_select.toggle() }
            }
        }
    }

    private class CategoriesDiffUtilCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
            oldItem.categoryId == newItem.categoryId

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
            oldItem == newItem
    }

}
