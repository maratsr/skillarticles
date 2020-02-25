package ru.skillbranch.skillarticles.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.markdown.spans.UnorderedListSpan

//import android.text.style.StrikethroughSpan
//import android.text.style.StyleSpan
//import android.text.style.URLSpan
//import androidx.core.text.buildSpannedString
//import androidx.core.text.inSpans
//import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
//import ru.skillbranch.skillarticles.extensions.dpToPx
//import ru.skillbranch.skillarticles.markdown.spans.*

class MarkdownBuilder(context: Context) {
    private val colorSecondary = context.attrValue(R.attr.colorSecondary)
//    private val colorPrimary = context.attrValue(R.attr.colorPrimary)
//    private val colorDivider = context.getColor(R.color.color_divider)
//    private val colorOnSurface = context.attrValue(R.attr.colorOnSurface)
//    private val colorSurface = context.attrValue(R.attr.colorSurface)
    private val gap: Float = context.dpToPx(8)
    private val bulletRadius = context.dpToPx(4)
//    private val strikeWidth = context.dpToPx(4)
//    private val headerMarginTop = context.dpToPx(12)
//    private val headerMarginBottom = context.dpToPx(8)
//    private val ruleWidth = context.dpToPx(2)
//    private val cornerRadius = context.dpToPx(8)
//    private val linkIcon = context.getDrawable(R.drawable.ic_link_black_24dp)!!
//
    fun markdownToSpan(string: String): SpannedString {
        val markdown = MarkdownParser.parse(string)
        return buildSpannedString {
            markdown.elements.forEach {
                buildElement(it, this)
            }
        }
    }

    private fun buildElement(element: Element, builder: SpannableStringBuilder): CharSequence {
        return builder.apply {
            when(element) {
                is Element.Text -> append(element.text)
                is Element.UnorderedListItem -> {
                    inSpans(UnorderedListSpan(gap, bulletRadius, colorSecondary)) {
                        for(child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                else -> append(element.text)
            }

        }
    }
}

