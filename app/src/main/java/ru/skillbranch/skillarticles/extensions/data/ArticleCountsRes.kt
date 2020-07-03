package ru.skillbranch.skillarticles.extensions.data

import ru.skillbranch.skillarticles.data.local.entities.ArticleCounts
import ru.skillbranch.skillarticles.data.remote.res.ArticleCountsRes
// Трансформация response ArticleCountsRes -> ArticleCounts
fun ArticleCountsRes.toArticleCounts(): ArticleCounts {
    return ArticleCounts(
        articleId = articleId,
        likes = likes,
        comments = comments,
        readDuration = readDuration
    )
}