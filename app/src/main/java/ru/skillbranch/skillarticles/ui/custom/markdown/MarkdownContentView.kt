package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
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

    // for restore
    private var ids = arrayListOf<Int>()

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
        val bodyWidth = right - left - paddingLeft - paddingRight
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
                        context,
                        textSize,
                        it.image.url,
                        it.image.text,
                        it.image.alt
                    )
                    addView(iv)
                }

                is MarkdownElement.Scroll -> {
                    val sv = MarkdownCodeView(
                        context,
                        textSize,
                        it.blockCode.text
                    )

                    addView(sv)
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


    public override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.viewModeFlags = SparseArray()
        children.forEach {view ->
            when(view) {
                is MarkdownCodeView -> savedState.viewModeFlags?.put(indexOfChild(view), view.getMode())
                is MarkdownImageView -> savedState.viewModeFlags?.put(indexOfChild(view), view.tv_alt?.isVisible ?: false)
            }
        }
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            children.forEach {view ->
                when(view) {
                    is MarkdownCodeView -> view.setMode(state.viewModeFlags!!.get(indexOfChild(view)))
                    is MarkdownImageView -> view.tv_alt?.isVisible = state.viewModeFlags!!.get(indexOfChild(view))
                }
            }
        } else super.onRestoreInstanceState(state)
    }

    //Утащил идеи с https://medium.com/@kirillsuslov/how-to-save-android-view-state-in-kotlin-9dbe96074d49
    // https://medium.com/@mattcarroll/android-how-to-save-state-in-a-custom-view-30e5792c584b
    internal class SavedState : BaseSavedState {
        var viewModeFlags : SparseArray<Boolean>? = null

        constructor(source: Parcel) : super(source) {
            viewModeFlags = source.readSparseArray(javaClass.classLoader)
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSparseArray(viewModeFlags)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}