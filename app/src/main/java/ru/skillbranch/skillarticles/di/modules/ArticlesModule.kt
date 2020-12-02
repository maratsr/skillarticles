package ru.skillbranch.skillarticles.di.modules

import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import android.R
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.data.repositories.IRepository
import ru.skillbranch.skillarticles.ui.articles.ArticlesFragment
import ru.skillbranch.skillarticles.ui.articles.IArticlesView


@InstallIn(FragmentComponent::class) // установим во фрагменты
@Module
abstract class ArticlesModule {
    @Binds
    abstract fun bindArticleRepository(repo: ArticlesRepository): IRepository

     // Привяжем интерфейс к реализации
    @Binds
    abstract fun bindClickListener(fragment: ArticlesFragment): IArticlesView

    companion object {
        @Provides // Приведение типов как подсказка Hilt-у для bindClickListener
        fun provideArticlesFragment(fragment: Fragment): ArticlesFragment = fragment as ArticlesFragment

        @Provides
        fun provideSimpleCursorAdapter(fragment: Fragment): SimpleCursorAdapter = // Забиндить на системное View=android.R.layout.simple_list_item_1 данные из SQL курсора
            SimpleCursorAdapter(
                fragment.context,
                R.layout.simple_list_item_1,
                null, //cursor
                arrayOf("tag"), // Значение полей курсора, связанных с View
                intArrayOf(R.id.text1), // К какому идентификатору привязываем
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER // регистрируем наблюдателя за контентом
            )
    }
}