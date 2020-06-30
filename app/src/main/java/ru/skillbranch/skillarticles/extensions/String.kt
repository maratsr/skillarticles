package ru.skillbranch.skillarticles.extensions


// Реализуй функцию расширения fun String.indexesOf(substr: String, ignoreCase: Boolean = true): List,
// в качестве аргумента принимает подстроку и флаг - учитывать или нет регистр подстроки при поиске по исходной строке.
// Возвращает список позиций вхождений подстроки в исходную строку. Пример: "lorem ipsum sum".indexesOf("sum") // [8, 12]
fun String?.indexesOf( substr: String, ignoreCase: Boolean = true): List<Int> {
    val result = mutableListOf<Int>()
    if (!this.isNullOrEmpty() && substr.isNotEmpty()) {
        var index = 0
        while (index > -1) {
            index = indexOf(substr, index, ignoreCase)
            if (index > -1) {
                result.add(index)
                index += substr.length
            }
        }
    }
    return result
}