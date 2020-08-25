package ru.skillbranch.skillarticles.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import ru.skillbranch.skillarticles.data.remote.res.ArticleRes

interface RestService {
    //https://skill-articles.skill-branch.ru/api/v1/articles?last=articleId&limit=10
    @GET("articles") // возвращает список статей
    suspend fun articles(
        @Query("last") last: String? = null,
        @Query("limit") limit: Int = 10
    ): List<ArticleRes>
}