package ru.skillbranch.skillarticles

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.skillbranch.skillarticles.data.local.AppDb
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.Author
import java.util.*

@RunWith(AndroidJUnit4::class)
class RoomTests {
    private lateinit var testDb:AppDb

    @Before
    fun createDb() {
        testDb= Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDb::class.java
        ).build()
    }

    @After
    fun closeDb(){
        testDb.close()
    }

    @Test
    fun test_insert_one() {
        val expectedArticle = Article(id="0", title="test article", description="test description", categoryId="0", poster="anyurl",
            //updatedAt = Date(0L ), date = Date(0L),
            author = Author("1","0","0"))
        testDb.articlesDao().insert(expectedArticle) // для записи реальной СУБД = db.articlesDao().insert(expectedArticle)
        val actualArticle = testDb.articlesDao().findArticleById(expectedArticle.id)
        assertEquals("EqualOrNot:",expectedArticle, actualArticle)

    }

    @Test
    fun test_insert_many() {
        var expectedArticles = mutableListOf<Article>()
        val expectedArticle = Article(id="0", title="test article", description="test description", categoryId="0", poster="anyurl",
            //updatedAt = Date(0L ), date = Date(0L),
            author = Author("1","0","0"))
        repeat(10) {
            expectedArticles.add(expectedArticle.copy(id=it.toString()))
        }

        testDb.articlesDao().insert(expectedArticles)
        val actualArticles = testDb.articlesDao().findArticles()
        assertEquals("EqualOrNot:",expectedArticles, actualArticles)

    }

}