package ru.skillbranch.skillarticles.markdown

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\n" // System.getProperty("line.separator") ?: \n

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
    private const val BLOCK_CODE_GROUP = "" //TODO implement me
    private const val ORDER_LIST_GROUP = "" //TODO implement me

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP"
//    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
//            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP"
    //|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP optionally

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): MarkdownText { // Парсит элементы разметки
        //TODO implement me
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
        //TODO implement me
        return null // Строка без markdown символов (для поиска по тексту)
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        //TODO implement me
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@while(matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            // Если нашелся markdown маркер - то до него просто текст
            if(lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            var text: CharSequence
            val groups = 1..9 // Смотрим под какие типы markdown-а подходит
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
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    // next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }
                2 -> { // header
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length // уровень заголовка = кол-ву #
                    text = string.subSequence(startIndex.plus(level.inc()),endIndex) // Текст после ###
                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                3 -> { // quotes (цитаты)
                    text = string.subSequence(startIndex.plus(2),endIndex) // Текст после >
                    val subelements = findElements(text)

                    val element = Element.Quote(text, subelements) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                4 -> { // italic
                    text = string.subSequence(startIndex.inc(),endIndex.dec()) // Текст между _ или *
                    val subelements = findElements(text)

                    val element = Element.Italic(text, subelements) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                5 -> { // bold
                    text = string.subSequence(startIndex.plus(2),endIndex.plus(-2)) // Текст между __ или **
                    val subelements = findElements(text)

                    val element = Element.Bold(text, subelements) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                6 -> { // strike
                    text = string.subSequence(startIndex.plus(2),endIndex.plus(-2)) // Текст между ~~
                    val subelements = findElements(text)

                    val element = Element.Strike(text, subelements) // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                7 -> { // rule
                    val element = Element.Rule() // Элемент из текста и подэлементы
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                8 -> { // inline code
                    text = string.subSequence(startIndex.inc(),endIndex.dec()) // Текст между `

                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                9 -> { // link
                    text = string.subSequence(startIndex,endIndex) // Текст между [title](link)
                    val (title:String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }
        }

        if(lastStartIndex<string.length) { // проверка после последнего вхождения, если там что то есть - то простой текст
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents

        /*
        loop@ while () {
            //TODO implement me
            //groups range for iterate by groups (1..9) or (1..11) optionally
            val groups = 1..11
            when () {
                //NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    //TODO implement me
                }

                //HEADER
                2 -> {
                    //text without "{#} "
                    //TODO implement me
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    //TODO implement me
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    //TODO implement me
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    //TODO implement me
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    //TODO implement me
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    //TODO implement me
                }

                //RULE
                8 -> {
                    //text without "`{}`"
                    //TODO implement me
                }

                //LINK
                9 -> {
                    //full text for regex
                    //TODO implement me
                }
                //10 -> BLOCK CODE - optionally
                10 -> {
                    //TODO implement me
                }

                //11 -> NUMERIC LIST
                11 -> {
                    //TODO implement me
                }
            }

        }
        */
        //TODO implement me
    }
}

// Обертка - список элементов
data class MarkdownText(val elements: List<Element>)

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
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}