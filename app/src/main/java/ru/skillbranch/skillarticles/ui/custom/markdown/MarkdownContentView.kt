package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.forEach
import androidx.core.util.isEmpty
import androidx.core.view.ViewCompat
import androidx.core.view.children
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.groupByBounds
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import kotlin.properties.Delegates

class MarkdownContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private lateinit var elements: List<MarkdownElement>
   // private var layoutManager: LayoutManager = LayoutManager()
    companion object {
        public var layoutManager = LayoutManager()
    }

    // Отслеживает изменения получения нового значения, по окончании - запускает лямбду (название свойства, старое, новое значение)
    var textSize by Delegates.observable(14f) { _, old, value ->
        if (value == old) return@observable
        this.children.forEach {
            // Всем дочерним устанавливаем новое значение
            it as IMarkdownView
            it.fontSize = value
        }
    }

    var isLoading: Boolean = true
    val padding = context.dpToIntPx(8)


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = paddingTop
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        children.forEach {
            // Перебираем потомков и увеличиваем текущую высоту
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            usedHeight += it.measuredHeight
        }

        usedHeight += paddingBottom // учитываем паддинг
        setMeasuredDimension(width, usedHeight) // устанавливаем размер корневой viewgroup-ы
    }

    // Компоновка ViewGroup-ы
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = right - left - paddingLeft- paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        children.forEach {
            // Выставляем границы дочерних элементов
            if (it is MarkdownTextView) {
                it.layout(
                    left - paddingLeft / 2, usedHeight,
                    r - paddingRight / 2, usedHeight + it.measuredHeight
                )
            } else {
                it.layout(left, usedHeight, right, usedHeight + it.measuredHeight)
            }
            usedHeight += it.measuredHeight
        }
    }

    fun setContent(content: List<MarkdownElement>) {
        elements = content
        var index = 0
        content.forEach {
            when (it) {
                is MarkdownElement.Text -> {
                    val tv = MarkdownTextView(context, textSize).apply {
                        setPaddingOptionally(left = padding, right = padding)
                        setLineSpacing(fontSize * 0.5f, 1f) // Местрочный интервал
                    }

                    MarkdownBuilder(context)
                        .markdownToSpan(it)
                        .run {
                            tv.setText(this, TextView.BufferType.SPANNABLE)
                        }

                    addView(tv)
                }

                is MarkdownElement.Image -> {
                    val iv = MarkdownImageView(
                        context, textSize, it.image.url, it.image.text, it.image.alt)
                    addView(iv)
                    layoutManager.attachToParent(iv, index)
                    index++

                }

                is MarkdownElement.Scroll -> {
                    val sv = MarkdownCodeView(
                        context, textSize, it.blockCode.text)
                    addView(sv)
                    layoutManager.attachToParent(sv, index)
                    index++
                }
            }
        }
    }

    // В какой child view искать и кула передать правильные смещения
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        // searchResult - границы (первый и последний символ найденных подстрок) в тексте результатов поиска
        children.forEach {view ->
            view as IMarkdownView
            view.clearSearchResult()
        }

        if(searchResult.isEmpty()) return

        // Bounds - границы контента View
        val bounds = elements.map { it.bounds }
        // Список1 из списков2. 1- дочерние View родительской ViewGroup, внутри которого список2-
        // границы (первый и последний символ найденных подстрок) в контексте дочернего результатов поиска
        val result = searchResult.groupByBounds(bounds)

        children.forEachIndexed { index, view ->
            view as IMarkdownView
            // search for child view with markdown element offset
            view.renderSearchResult(result[index], elements[index].offset)
        }

    }

    fun renderSearchPosition(searchPosition: Pair<Int, Int>?, force: Boolean = false) {
        searchPosition ?: return
        val bounds = elements.map { it.bounds }
        val index = bounds.indexOfFirst { (start, end) ->
            val boundRange = start..end
            val (startPos, endPos) = searchPosition
            startPos in boundRange && endPos in boundRange
        }

        if (index == -1) return
        val view = getChildAt(index)
        view as IMarkdownView
        view.renderSearchPosition(searchPosition, elements[index].offset)
    }


    fun clearSearchResult() {
        children.forEach { view ->
            view as IMarkdownView
            view.clearSearchResult()
        }
    }


    fun setCopyListener(listener: (String) -> Unit) {
        children.filterIsInstance<MarkdownCodeView>() // Если MarkdownCodeView
            .forEach { it.copyListener = listener }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = SavedState(super.onSaveInstanceState())
        state.layout = layoutManager
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
        if (state is SavedState)
            layoutManager = state.layout
    }

    // Сохраняем состояние дочерних представлений
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        //save children manually without markdown text view
        children.filter { it !is MarkdownTextView } // Сохраняем представления только MarkdownImageView и CodeView
            .forEach { it.saveHierarchyState(layoutManager.container) }
        //save only markdownContentView
        dispatchFreezeSelfOnly(container)
    }

    class LayoutManager() : Parcelable { // Контейнер для хранения идентификаторов дочерних представлений
        var ids: MutableList<Int> = mutableListOf()
        var container: SparseArray<Parcelable> = SparseArray() // состояния дочерних элементов

        constructor(parcel: Parcel) : this() {
            ids = parcel.readArrayList(Int::class.java.classLoader) as ArrayList<Int>
            container =
                parcel.readSparseArray<Parcelable>(this::class.java.classLoader) as SparseArray<Parcelable>
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeIntArray(ids.toIntArray())
            parcel.writeSparseArray(container)
        }

        fun attachToParent(view: View, index: Int) {
            if (container.isEmpty()) {
                view.id = ViewCompat.generateViewId()
                ids.add(view.id)
            } else {
                view.id = ids[index]
                view.restoreHierarchyState(container)
            }
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<LayoutManager> {
            override fun createFromParcel(parcel: Parcel): LayoutManager = LayoutManager(parcel)
            override fun newArray(size: Int): Array<LayoutManager?> = arrayOfNulls(size)
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        lateinit var layout: LayoutManager

        constructor(superState: Parcelable?) : super(superState)

        @Suppress("UNCHECKED_CAST")
        constructor(src: Parcel) : super(src) {
            //restore state from parcel
            layout = src.readParcelable(LayoutManager::class.java.classLoader)!!
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            //write state to parcel
            super.writeToParcel(dst, flags)
            dst.writeParcelable(layout, flags)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}