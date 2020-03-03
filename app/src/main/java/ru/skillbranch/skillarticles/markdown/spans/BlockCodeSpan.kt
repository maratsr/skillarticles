package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.*
import android.text.SpannableString
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.markdown.Element


class BlockCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    var measureWidth: Int = 0
    var oldAscent = 0
    var oldDescent = 0
    var oldFm: Paint.FontMetricsInt? = null

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        when(type) {
            Element.BlockCode.Type.SINGLE -> paint.forBackground {
                rect.set(0f , top.toFloat() + padding, x + canvas.width,bottom.toFloat() - padding)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }

            Element.BlockCode.Type.START -> paint.forBackground {
                rect.set(0f , top.toFloat() + padding,x + canvas.width , bottom.toFloat())
                path.reset()
                path.addRoundRect(rect, floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,0f ,0f,0f,0f), Path.Direction.CW)
                canvas.drawPath(path, paint)
            }

            Element.BlockCode.Type.MIDDLE -> paint.forBackground {
                rect.set(0f , top.toFloat(), x + canvas.width , bottom.toFloat())
                canvas.drawRect(rect, paint)
            }

            Element.BlockCode.Type.END -> paint.forBackground {
                rect.set(0f , top.toFloat(), x + canvas.width,bottom.toFloat() - padding)
                path.reset()
                path.addRoundRect(rect, floatArrayOf(0f, 0f, 0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius), Path.Direction.CW)
                canvas.drawPath(path, paint)
            }
        }
        paint.forText {
            canvas.drawText(text as SpannableString, start, end, x + padding, y.toFloat(), paint)
        }
        oldFm?.ascent = oldAscent
        oldFm?.descent = oldDescent
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            oldFm = fm
            oldAscent = fm.ascent
            oldDescent = fm.descent
            when (type) {
                Element.BlockCode.Type.SINGLE -> {
                    fm.ascent = (oldAscent*0.85 - 2 * padding).toInt()
                    fm.descent = (oldDescent*0.85 + 2 * padding).toInt()
                }

                Element.BlockCode.Type.START -> {
                    fm.ascent = (oldAscent*0.85 - 2 * padding).toInt()
                    fm.descent = (oldDescent*0.85).toInt()
                }

                Element.BlockCode.Type.MIDDLE -> {
                    fm.ascent = (oldAscent * 0.85).toInt()
                    fm.descent = (oldDescent * 0.85).toInt()
                }

                Element.BlockCode.Type.END -> {
                    fm.ascent = (oldAscent * 0.85).toInt()
                    fm.descent = (oldDescent * 0.85 + 2 * padding).toInt()
                }
            }
        }
        return 0
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0
        val oldFont = typeface
        val oldColor = color

        color = textColor
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *= 0.85f

        block()

        // Восстановим старые настройки
        color= oldColor
        typeface = oldFont
        textSize = oldSize
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldStyle = style
        val oldColor = color

        color = bgColor
        style = Paint.Style.FILL
        block()
        // Восстановим старые настройки
        color = oldColor
        style = oldStyle
    }
}
