package ru.skillbranch.skillarticles.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.viewmodels.article.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [ChoseCategoryDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChoseCategoryDialog : DialogFragment() {
    private val viewModel : ArticlesViewModel by activityViewModels() // shared viewModel для 3х фрагментов
    private val selectedCategories = mutableListOf<String>()
    private val args : ChoseCategoryDialogArgs by  navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //TODO save checked state and implement cusom items
        val categories = args.categories.toList().map{"${it.title} (${it.articlesCount})"}.toTypedArray() // Собственно элементы
        val checked = BooleanArray(args.categories.size) { args.selectedCategories.contains(args.categories[it].categoryId)} // Какие из элементов выделены

        val adb = AlertDialog.Builder(requireContext())
            .setTitle("Chose category")
            .setPositiveButton("Apply") {_, _ ->
                viewModel.applyCategories(selectedCategories)
            }
            .setNegativeButton("Reset") {_, _ ->
                viewModel.applyCategories(emptyList())
            }
            .setMultiChoiceItems(categories, checked) { dialog, which, isChecked ->
                if (isChecked)
                    selectedCategories.add(args.categories[which].categoryId)
                else
                    selectedCategories.remove(args.categories[which].categoryId)
            }

        return adb.create()
    }
}
