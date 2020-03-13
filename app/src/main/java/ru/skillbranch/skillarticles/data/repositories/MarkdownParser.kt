package ru.skillbranch.skillarticles.data.repositories

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR =  System.getProperty("line.separator") ?: "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)" // ненумерованный список имеет формать +-* пробел текст
    private const val HEADER_GROUP = "(^#{1,6} .+?$)" // заголовки, стартующие с 1 до 6 символов #
    private const val QUOTE_GROUP = "(^> .+?\$)" // цитирование начинается с символа >
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))" // italic - окружение текста символами _ или *
    private const val BOLD_GROUP ="((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))" // bold - окружение текста символами __ или **
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?~{2}(?!~))" //strike - окружение текста символами ~~
    private const val RULE_GROUP = "(^[-_*]{3}$)" // --- ___ *** - горизонтальный разделитель
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))" //текст окруженный ` штрихом)
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|\\[*?]\\(.*?\\))"  // ссылка [title](url) [I`am yandex link](https://www.yandex.ru)
    private const val BLOCK_CODE_GROUP = "(^```[\\S\\s]+?```)"//"(^```[\\s\\S]+```$)"
    private const val ORDER_LIST_GROUP = "(^\\d{1,2}\\.\\s.+?$)"
    private const val IMAGE_GROUP = "(^!\\[[^\\[\\]]*?\\]\\(.*?\\)$)" // изображение в формате ![alt](url "title")

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP|$IMAGE_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     * Возвращает укрупненные Markdown элементы (текст, image или scroll)
     */
    fun parse(string: String): List<MarkdownElement> { // Парсит элементы разметки
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return elements.fold(mutableListOf()) { acc, el ->
            val last = acc.lastOrNull()
            when (el) {
                is Element.Image -> acc.add(MarkdownElement.Image(el, last?.bounds?.second ?: 0))
                is Element.BlockCode -> acc.add(MarkdownElement.Scroll(el, last?.bounds?.second ?: 0))
                else -> {
                    if (last is MarkdownElement.Text) last.elements.add(el)
                    else acc.add(MarkdownElement.Text(mutableListOf(el), last?.bounds?.second ?: 0))
                }
            }
            acc
        }
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
        string ?: return null
        var clearedString = ""
        for(elm in findElements(
            string
        ))
            clearedString += getSimple(
                elm
            )
        return clearedString // Строка без markdown символов (для поиска по тексту)
    }

    private fun getSimple(element: Element): String {
        var bufferString = ""
        for(e in element.elements)
            bufferString += getSimple(
                e
            )
        return if (element.elements.isEmpty()) element.text.toString() else bufferString
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@while(matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            // Если нашелся markdown маркер - то до него просто текст
            if(lastStartIndex < startIndex) {
                parents.add(
                    Element.Text(
                        string.subSequence(lastStartIndex, startIndex)
                    )
                )
            }

            var text: CharSequence
            val groups = 1..12 // Смотрим под какие типы markdown-а подходит
            var group = -1
            for(gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }
            when(group) {
                -1 -> break@loop // not found, break
                1 -> { // unordered list
                    // Смещаемся на 2 символа, так как ненумерованный список начинается +-* и пробела
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    // Вложенные элементы (между первым и последним вхождением
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.UnorderedListItem(
                            text,
                            subs
                        )
                    parents.add(element)

                    // next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }
                2 -> { // header
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length // уровень заголовка = кол-ву #
                    text = string.subSequence(startIndex.plus(level.inc()),endIndex) // Текст после ###
                    val element =
                        Element.Header(
                            level,
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                3 -> { // quotes (цитаты)
                    text = string.subSequence(startIndex.plus(2),endIndex) // Текст после >
                    val subelements =
                        findElements(
                            text
                        )

                    val element =
                        Element.Quote(
                            text,
                            subelements
                        ) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                4 -> { // italic
                    text = string.subSequence(startIndex.inc(),endIndex.dec()) // Текст между _ или *
                    val subelements =
                        findElements(
                            text
                        )

                    val element =
                        Element.Italic(
                            text,
                            subelements
                        ) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                5 -> { // bold
                    text = string.subSequence(startIndex.plus(2),endIndex.plus(-2)) // Текст между __ или **
                    val subelements =
                        findElements(
                            text
                        )

                    val element =
                        Element.Bold(
                            text,
                            subelements
                        ) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                6 -> { // strike
                    text = string.subSequence(startIndex.plus(2),endIndex.plus(-2)) // Текст между ~~
                    val subelements =
                        findElements(
                            text
                        )

                    val element =
                        Element.Strike(
                            text,
                            subelements
                        ) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                7 -> { // rule
                    val element =
                        Element.Rule() // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                8 -> { // inline code
                    text = string.subSequence(startIndex.inc(),endIndex.dec()) // Текст между `

                    val element =
                        Element.InlineCode(
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                9 -> { // link
                    text = string.subSequence(startIndex,endIndex) // Текст между [title](link)
                    val (title:String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element =
                        Element.Link(
                            link,
                            title
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                //10 -> BLOCK CODE - optionally
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3)).toString()
                    val element = Element.BlockCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //11 -> NUMERIC LIST
                11 -> {
                    val reg = "(^\\d{1,2}.)".toRegex().find(string.substring(startIndex, endIndex))
                    val order = reg!!.value
                    text = string.subSequence(startIndex.plus(order.length.inc()), endIndex).toString()
                    val subs = findElements( text)
                    parents.add(
                        Element.OrderedListItem(
                            order,
                            text.toString(),
                            subs
                        )
                    )
                    lastStartIndex = endIndex
                }
                // IMAGE
                12 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt, url, title) = "^!\\[([^\\[\\]]*?)?]\\((.*?) \"(.*?)\"\\)$".toRegex()
                        .find(text)!!.destructured

                    val element = Element.Image(url, alt, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }
        }

        if(lastStartIndex<string.length) { // проверка после последнего вхождения, если там что то есть - то простой текст
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(
                Element.Text(
                    text
                )
            )
        }
        return parents
    }
}

// Обертка - список элементов
data class MarkdownText(val elements: List<Element>)

// Одна View состоящая из комб 3 разныз View: Текст из кучи span-ов + ImageViewGroup (состоит из ImageView, Title) +
// горизонтальный scroll (для листинга длинных строк)
sealed class MarkdownElement {
    abstract val offset: Int // Смещение от начала View
    val bounds: Pair<Int, Int> by lazy { // Границы  View содержащие исходный текст (границы текста, фактически [offset... offset + размер текстовой части View]
        when(this){
            is Text -> {
                val end = elements.fold(offset) { acc, el ->
                    acc + el.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> offset to image.text.length + offset
            is Scroll -> offset to blockCode.text.length + offset
        }
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ): MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ): MarkdownElement()

    data class Scroll(
        val blockCode: Element.BlockCode,
        override val offset: Int = 0
    ): MarkdownElement()
}

sealed class Element() { // Соответствует элементу markdown разметки
    abstract val text: CharSequence
    abstract val elements: List<Element> // дочерние подэлементы

    data class Text( // Элемент markdown - просто текст
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem( //Элемент markdown - ненумерованный список
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote( // Цитаты
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span (пустой элемент, чтобы прикрепить span)
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String, // Собственно ссылка
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem( // нумерованный список
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
        val url: String,
        val alt: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()
}

private fun Element.spread() : List<Element>{ // Возврашает список элементов
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    forEach { elements.addAll(it.spread()) }
    return elements
}

private fun Element.clearContent(): String { // Очистка от markdown символов
    return StringBuilder().apply {
        val element = this@clearContent
        if(element.elements.isEmpty()) append(element.text)
        else element.elements.forEach { append(it.clearContent()) }
    }.toString()
}

fun List<MarkdownElement>.clearContent(): String { // Возвращает строку, очищеннюу от markdown символов
    return StringBuilder().apply {
        this@clearContent.forEach {
            when (it) {
                is MarkdownElement.Text -> it.elements.forEach { el -> append(el.clearContent()) }
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())
            }
        }
    }.toString()
}