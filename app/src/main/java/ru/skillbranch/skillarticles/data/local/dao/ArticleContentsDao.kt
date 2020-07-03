package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import ru.skillbranch.skillarticles.data.local.entities.ArticleContent

@Dao
interface ArticleContentsDao: BaseDao<ArticleContent> {
}