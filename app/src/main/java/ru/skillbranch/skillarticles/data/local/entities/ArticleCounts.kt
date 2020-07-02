package ru.skillbranch.skillarticles.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

// Числовые данные к статье (кол-во комментариев, like-ов...)
@Entity(
    tableName = "article_counts",
    foreignKeys = [ForeignKey(
        entity = Article::class, // Основаня ссылаемая сущность
        parentColumns = ["id"], // Ссылается на внешнюю таблицу
        childColumns = ["article_id"], // Ключевое поле
        onDelete = ForeignKey.CASCADE
    )]
)
data class ArticleCounts(
    @PrimaryKey
    @ColumnInfo(name = "article_id")
    val articleId: String,
    val likes: Int = 0,
    val comments: Int = 0,
    @ColumnInfo(name = "read_duration")
    val readDuration: Int = 0,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)