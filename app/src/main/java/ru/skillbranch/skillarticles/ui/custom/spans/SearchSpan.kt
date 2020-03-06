package ru.skillbranch.skillarticles.ui.custom.spans

import android.text.style.ForegroundColorSpan

// Используется для определения позиции (перерисовка цвета paint-а)
open class SearchSpan(fgColor: Int) : ForegroundColorSpan(fgColor)