package ru.skillbranch.skillarticles.data.local

import androidx.room.TypeConverter
import java.util.*

// Конвертеры типов полей для Room
class DateConverter{
    @TypeConverter
    fun timestampToDate(timestamp:Long):Date = Date(timestamp)

    @TypeConverter
    fun dateToTimestamp(date:Date):Long = date.time
}