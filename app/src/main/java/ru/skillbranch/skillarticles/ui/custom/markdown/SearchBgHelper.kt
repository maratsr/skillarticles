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
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan

// Отрисовка фона под TextView
class SearchBgHelper(
    context: Context,
    private val focueListener: (Int) -> Unit // лямбда за фокус
) {
    private val padding: Int = context.dpToIntPx(4)
    private val radius: Float = context.dpToPx(8) // радиус скругления
    private val borderWidth: Int = context.dpToIntPx(1) // толщина границы

    private val secondaryColor = context.attrValue(R.attr.colorSecondary) // цвет фона
    private val alphaColor = ColorUtils.setAlphaComponent(secondaryColor, 160) // прозрачность фона под текстом

    // Drawable создаются lazy програмно, что нагляднее и немного быстрее из XML разметки

    // drawable для выделения в середине текста (все углы скруглены)
    // some text DRAWABLE continue sometext
    val drawable: Drawable by lazy {
        GradientDrawable(). apply {
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
        GradientDrawable(). apply {
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
        GradientDrawable(). apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(alphaColor) // полупрозрачность
            setStroke(borderWidth, secondaryColor) // ширина обводки и цвет
        }
    }

    // скругления только на правой стороне прямоугольника выделения
    val drawableRight: Drawable by lazy {
        GradientDrawable(). apply {
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

    private var spanEnd = 0
    private var spanStart = 0
    private var startLine = 0
    private var endLine = 0

    fun draw(canvas: Canvas, text: Spanned, layout: Layout) {
        spans = text.getSpans()
        spans.forEach {
            spanEnd = text.getSpanEnd(it)
            spanStart = text.getSpanStart(it)
            startLine = layout.getLineForOffset(spanStart)
            endLine = layout.getLineForOffset(spanEnd)
        }
    }

}
