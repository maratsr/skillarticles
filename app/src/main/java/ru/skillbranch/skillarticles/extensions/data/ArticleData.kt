package ru.skillbranch.skillarticles.extensions.data
//
//
//import ru.skillbranch.skillarticles.data.models.ArticleData
//import ru.skillbranch.skillarticles.data.remote.res.ArticleContentRes
//// Трансформация response ArticleData -> ArticleContentRes
//fun ArticleData.toArticleContentRes() : ArticleContentRes = ArticleContentRes(
//    articleId = id,
//    content = content,
//    source = source,
//    shareLink = shareLink)