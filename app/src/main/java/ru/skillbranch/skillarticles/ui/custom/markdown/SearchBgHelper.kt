package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Layout
import android.text.Spanned
import androidx.annotation.StringDef
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.Constraints
import androidx.core.graphics.ColorUtils
import androidx.core.text.getSpans
import org.intellij.lang.annotations.Pattern
import org.jetbrains.annotations.NotNull
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.data.getLineBottomWithoutPadding
import ru.skillbranch.skillarticles.extensions.data.getLineTopWithoutPadding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.spans.HeaderSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan

// Отрисовка фона под TextView
class SearchBgHelper(
    context: Context,
    private val focusListener: ((Int, Int) -> Unit)? = null, // лямбда за фокус c параметрами top, bottom
    mockDrawable: Drawable? = null //for mock drawable - костыль тестирования =(
) {

    constructor(context: Context, focusListener: ((Int, Int) -> Unit)) : this(context, focusListener, null)

    private val padding: Int = context.dpToIntPx(4)
    private val radius: Float = context.dpToPx(8) // радиус скругления
    private val borderWidth: Int = context.dpToIntPx(1) // толщина границы

    private val secondaryColor = context.attrValue(R.attr.colorSecondary) // цвет фона
    private val alphaColor = ColorUtils.setAlphaComponent(secondaryColor, 160) // прозрачность фона под текстом

    // Drawable создаются lazy програмно, что нагляднее и немного быстрее из XML разметки

    // drawable для выделения в середине текста (все углы скруглены)
    // some text DRAWABLE continue sometext
    val drawable: Drawable by lazy {
        mockDrawable ?: GradientDrawable(). apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = FloatArray(8).apply {fill(radius,0, size) }
            color = ColorStateList.valueOf(alphaColor) // полупрозрачность
            setStroke(borderWidth, secondaryColor) // ширина обводки и цвет
        }
    }

    // drawable для многострочного выделения:
    // some text some  DRAWABLELEFT
    // DRAWABLEMIDDLE DRAWABEMIDDLE
    // DRAWABLERIGHT some text

    // скругления только на левой стороне прямоугольника выделения
    val drawableLeft: Drawable by lazy {
        mockDrawable ?: GradientDrawable(). apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf( radius, radius, // top left radius in px
                0f, 0f,                                 // top right radius in px
                0f, 0f,                                 // bottom right radius in px
                radius, radius                          // bottom left radius in px
            )
            color = ColorStateList.valueOf(alphaColor) // полупрозрачность
            setStroke(borderWidth, secondaryColor) // ширина обводки и цвет
        }
    }

    // без скруглений прямоугольника выделения
    val drawableMiddle: Drawable by lazy {
        mockDrawable ?: GradientDrawable(). apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(alphaColor) // полупрозрачность
            setStroke(borderWidth, secondaryColor) // ширина обводки и цвет
        }
    }

    // скругления только на правой стороне прямоугольника выделения
    val drawableRight: Drawable by lazy {
        mockDrawable ?: GradientDrawable(). apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf( 0f, 0f, // top left radius in px
                radius, radius,                 // top right radius in px
                radius, radius,                  // bottom right radius in px
                0f, 0f                          // bottom left radius in px
            )
            color = ColorStateList.valueOf(alphaColor) // полупрозрачность
            setStroke(borderWidth, secondaryColor) // ширина обводки и цвет
        }
    }

    private lateinit var spans: Array<out SearchSpan>
    private lateinit var headerSpans: Array<out HeaderSpan>
    private lateinit var render: SearchBgRender
    private val singleLineRender: SearchBgRender =  SingleLineRender(padding, drawable)  // by lazy {SingleLineRender(padding, drawable)}
    private val multiLineRender: SearchBgRender = MultiLineRender(padding, drawableLeft, drawableMiddle, drawableRight) //by lazy { MultiLineRender(padding, drawableLeft, drawableMiddle, drawableRight)}

    private var spanEnd = 0
    private var spanStart = 0
    private var startLine = 0
    private var endLine = 0
    private var startOffset = 0
    private var endOffset = 0

    private var topExtraPadding = 0
    private var bottomExtraPadding = 0

    fun draw(canvas: Canvas, text: Spanned, layout: Layout) {
        // Перебираем спаны по циклу
        spans = text.getSpans()
        spans.forEach {
            spanEnd = text.getSpanEnd(it)
            spanStart = text.getSpanStart(it)
            startLine = layout.getLineForOffset(spanStart) // возвращает номер строки
            endLine = layout.getLineForOffset(spanEnd) // номер строки окончания спана

            if (it is SearchFocusSpan) { // Переводим фокус на эту высоту строки
                focusListener?.invoke(layout.getLineTop(startLine), layout.getLineBottom(startLine))
            }

            headerSpans = text.getSpans(spanStart, spanEnd, HeaderSpan::class.java)
            topExtraPadding = 0
            bottomExtraPadding = 0

            // Заголовки обработаем отдельно чтобы уменьшить высоту выделения
            if (headerSpans.isNotEmpty()) {
                topExtraPadding = if(spanStart in headerSpans[0].firstLineBounds ||  spanEnd in headerSpans[0].firstLineBounds)
                    headerSpans[0].topExtraPadding
                else 0

                bottomExtraPadding = if(spanStart in headerSpans[0].lastLineBounds ||  spanEnd in headerSpans[0].lastLineBounds)
                    headerSpans[0].bottomExtraPadding
                else 0


            }

            // Отступы от начала и конца строки (позиция по X)
            startOffset = layout.getPrimaryHorizontal(spanStart).toInt()
            endOffset = layout.getPrimaryHorizontal(spanEnd).toInt()

            render = if(startLine == endLine) singleLineRender else multiLineRender
            render.draw(canvas, layout, startLine, endLine, startOffset, endOffset, topExtraPadding, bottomExtraPadding)

        }
    }
}

abstract class SearchBgRender (val padding: Int) {
    abstract fun draw(canvas: Canvas, layout: Layout, startLine: Int, endLine: Int,
                      startOffset: Int, endOffset: Int, topExtraPadding: Int = 0, bottomExtraPadding: Int = 0)

    fun getLineTop(layout: Layout, line: Int): Int {
        return layout.getLineTopWithoutPadding(line)
    }

    fun getLineBottom(layout: Layout, line: Int): Int {
        return layout.getLineBottomWithoutPadding(line)
    }
}


class SingleLineRender(padding: Int, val drawable: Drawable) : SearchBgRender(padding) {

    private var lineTop : Int = 0
    private var lineBottom : Int = 0
    override fun draw(
        canvas: Canvas,
        layout: Layout,
        startLine: Int,
        endLine: Int,
        startOffset: Int,
        endOffset: Int,
        topExtraPadding: Int,
        bottomExtraPadding: Int
    ) {
        lineTop = getLineTop(layout, startLine) + topExtraPadding
        lineBottom = getLineBottom(layout, startLine) - bottomExtraPadding
        drawable.setBounds(startOffset - padding, lineTop, endOffset + padding, lineBottom)
        drawable.draw(canvas)
    }
}

class MultiLineRender(padding: Int,
                      val drawableLeft: Drawable, val drawableMiddle: Drawable, val drawableRight: Drawable) : SearchBgRender(padding) {

    private var lineTop : Int = 0
    private var lineBottom : Int = 0
    private var lineEndOffset: Int = 0
    private var lineStartOffset: Int = 0

    override fun draw(
        canvas: Canvas,
        layout: Layout,
        startLine: Int,
        endLine: Int,
        startOffset: Int,
        endOffset: Int,
        topExtraPadding: Int,
        bottomExtraPadding: Int
    ) {
        // отрисуем первую строку
        lineEndOffset = (layout.getLineRight(startLine) + padding).toInt() // где заканчивается строка
        lineTop = getLineTop(layout, startLine) + topExtraPadding
        lineBottom = getLineBottom(layout, startLine)
        drawStart(canvas, startOffset - padding, lineTop, lineEndOffset, lineBottom)


        // отрисуем промежуточные средние линии без закруглений
        for (line in startLine.inc() until endLine ) {
            lineTop = getLineTop(layout, line)
            lineBottom = getLineBottom(layout, line)
            drawableMiddle.setBounds(layout.getLineLeft(line).toInt() - padding, lineTop,
                layout.getLineRight(line).toInt() + padding, lineBottom)
            drawableMiddle.draw(canvas)

        }

        // отрисуем последнюю строку
        lineStartOffset = (layout.getLineLeft(startLine) - padding).toInt() // где заканчивается строка
        lineTop = getLineTop(layout, endLine)
        lineBottom = getLineBottom(layout, endLine) - bottomExtraPadding
        drawEnd(canvas, lineStartOffset, lineTop, endOffset+padding, lineBottom)

        //drawable.setBounds(startOffset, lineTop, endOffset, lineBottom)
        //drawable.draw(canvas)
    }

    private fun drawStart(canvas: Canvas, start: Int, top: Int, end: Int, bottom: Int) { // начала выделения скругленное слева
        drawableLeft.setBounds(start, top, end, bottom)
        drawableLeft.draw(canvas)
    }

    private fun drawEnd(canvas: Canvas, start: Int, top: Int, end: Int, bottom: Int) { // конец выделения скругленное справа
        drawableRight.setBounds(start, top, end, bottom)
        drawableRight.draw(canvas)
    }
}