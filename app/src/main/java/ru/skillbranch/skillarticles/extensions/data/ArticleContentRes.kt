package ru.skillbranch.skillarticles.extensions.data

import ru.skillbranch.skillarticles.data.local.entities.ArticleContent
import ru.skillbranch.skillarticles.data.remote.res.ArticleContentRes

// Трансформация response ArticleContentRes -> ArticleContent
fun ArticleContentRes.toArticleContent(): ArticleContent  = ArticleContent(
    articleId = articleId,
    content = content,
    source = source,
    shareLink = shareLink,
    updatedAt = updatedAt)