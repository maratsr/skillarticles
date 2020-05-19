package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.selectDestination
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify


class RootActivity : BaseActivity<RootViewModel>() {

//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    public override val binding: ArticleBinding by lazy { ArticleBinding() }

    override val layout: Int = R.layout.activity_root
    public override val viewModel: RootViewModel by viewModels ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // динамически создаем appBar и navController
        val appbarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_articles, R.id.nav_bookmarks, R.id.nav_transcriptions, R.id.nav_profile ))
        setupActionBarWithNavController(navController, appbarConfiguration)

//        nav_view.setupWithNavController(navController)
        nav_view.setOnNavigationItemSelectedListener {
            // if click on bottom navigation item -> navigate to destination by item.id
            viewModel.navigate(NavigationCommand.To(it.itemId))
            true
        }

        // Контроль перемещений между фрагментами
        navController.addOnDestinationChangedListener { controller, destination, arguments ->

            if (viewModel.currentState.isAuth && destination.id == R.id.nav_auth) {
                controller.popBackStack()
                viewModel.navigate(NavigationCommand.To(R.id.nav_profile, arguments))
            }

            // if destination changes set selected bottom navigation item
            nav_view.selectDestination(destination)
        }
    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(container, notify.message, Snackbar.LENGTH_LONG)
        snackbar.anchorView = findViewById<Bottombar>(R.id.bottombar) ?: nav_view

        when (notify) { // переберем варианты представителей sealed class-а
            is Notify.TextMessage -> {
                /* nothing */
            }

            is Notify.ActionMessage -> { // пример: при like статьи переспросить в snackbar-е с хендлом смены состояния на лайк
                val (_, label, handler) = notify
                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(label) {handler.invoke()}
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
