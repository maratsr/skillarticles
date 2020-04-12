package ru.skillbranch.skillarticles.ui.base

import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState

abstract class BaseFragment<T: BaseViewModel<out IViewModelState>>: Fragment() {
    // ссылка на владеющим им Activity
    val root: RootActivity
        get() = activity as RootActivity

    open val binding: Binding? = null
    protected abstract val viewModel: T
    protected abstract val layout: Int

    //set listeners, tuning views
    abstract fun setupViews()

    // Раздуем из xml разметки
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.restoreState()
        binding?.restoreUi(savedInstanceState)

        //следим за изменениями цикла фрагментов (отличается от цикла activity)
        viewModel.observeState(viewLifecycleOwner) { binding?.bind(it)}

        // bind default values if view model not loaded data
        if (binding?.isInflated == false) binding?.onFinishInflate()

        //Информируем rootActivity об изменениях
        viewModel.observeNotifications(viewLifecycleOwner) { root.renderNotification(it)}
        setupViews()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding?.rebind()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()
        binding?.saveUi(outState)
        super.onSaveInstanceState(outState)
    }
}