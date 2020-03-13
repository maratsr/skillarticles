package ru.skillbranch.skillarticles.data

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.skillbranch.skillarticles.R
import java.util.*

object LocalDataHolder {
    private var isDalay = true
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val articleData = MutableLiveData<ArticleData?>(null)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val articleInfo = MutableLiveData<ArticlePersonalInfo?>(null)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val settings = MutableLiveData(AppSettings())

    fun findArticle(articleId: String): LiveData<ArticleData?> {
        GlobalScope.launch {
            if (isDalay) delay(2000)
            articleData.postValue(
                ArticleData(
                    title = "CoordinatorLayout Basic",
                    category = "Android",
                    categoryIcon = R.drawable.logo,
                    date = Date(),
                    author = "Skill-Branch"
                )
            )
        }
        return articleData

    }

    fun findArticlePersonalInfo(articleId: String): LiveData<ArticlePersonalInfo?> {
        GlobalScope.launch {
            if (isDalay) delay(500)
            articleInfo.postValue(ArticlePersonalInfo(isBookmark = true))
        }
        return articleInfo
    }

    fun getAppSettings() = settings
    fun updateAppSettings(appSettings: AppSettings) {
        settings.value = appSettings
    }

    fun updateArticlePersonalInfo(info: ArticlePersonalInfo) {
        Log.e("DataHolder", "update personal info: $info");
        articleInfo.value = info
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun disableDelay() {
        isDalay = false
    }
}

object NetworkDataHolder {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val content = MutableLiveData<String?>(null)
    private var isDelay = true

    fun loadArticleContent(articleId: String): LiveData<String?> {
        GlobalScope.launch {
            if (isDelay) delay(500)
            content.postValue(longText)
        }
        return content
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun disableDelay() {
        isDelay = false
    }
}

data class ArticleData(
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: Date,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<Any> = emptyList()
)

data class ArticlePersonalInfo(
    val isLike: Boolean = false,
    val isBookmark: Boolean = false
)

data class AppSettings(
    val isDarkMode: Boolean = false,
    val isBigText: Boolean = false
)

//val longText: String = """
//    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas nibh sapien, consectetur et ultrices quis, convallis sit amet augue. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vestibulum et convallis augue, eu hendrerit diam. Curabitur ut dolor at justo suscipit commodo. Curabitur consectetur, massa sed sodales sollicitudin, orci augue maximus lacus, ut elementum risus lorem nec tellus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Praesent accumsan tempor lorem, quis pulvinar justo. Vivamus euismod risus ac arcu pharetra fringilla.
//
//Maecenas cursus vehicula erat, in eleifend diam blandit vitae. In hac habitasse platea dictumst. Duis egestas augue lectus, et vulputate diam iaculis id. Aenean vestibulum nibh vitae mi luctus tincidunt. Fusce iaculis molestie eros, ac efficitur odio cursus ac. In at orci eget eros dapibus pretium congue sed odio. Maecenas facilisis, dolor eget mollis gravida, nisi justo mattis odio, ac congue arcu risus sed turpis.
//
//Sed tempor a nibh at maximus. Nam ultrices diam ac lorem auctor interdum. Aliquam rhoncus odio quis dui congue interdum non maximus odio. Phasellus ut orci commodo tellus faucibus efficitur. Nulla congue nunc vel faucibus varius. Sed cursus ut odio ut fermentum. Vivamus mattis vel velit et maximus. Proin in sapien pharetra, ornare metus nec, dictum mauris.
//
//Praesent nisl nisl, iaculis id nulla in, congue eleifend leo. Sed aliquet elementum massa et gravida. Nulla facilisi. Cras convallis vestibulum elit et sodales. Cras consequat eleifend metus non tempus. Vivamus venenatis consequat mollis. Nunc suscipit ipsum at nunc dignissim porta. Sed accumsan tellus non mauris fermentum pulvinar. Morbi felis est, accumsan id est id, dictum interdum nulla. Proin ultrices, ante at placerat venenatis, tortor enim ullamcorper magna, at finibus nisi dui in ante. Vivamus convallis velit tortor, at mattis diam rhoncus vel. Integer at placerat turpis, vel laoreet nibh. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus fermentum tellus malesuada diam facilisis gravida. Quisque a semper ex, at semper dolor.
//
//In a turpis suscipit, venenatis arcu id, condimentum nulla. Mauris id felis id metus aliquet facilisis ut sit amet lectus. In aliquam dapibus mollis. Morbi sollicitudin purus ultricies dictum feugiat. Morbi lobortis mollis faucibus. Nunc mattis nec est sagittis semper. Curabitur in dignissim elit.
//""".trimIndent()
val longText: String = """
# Drawing a rounded corner background on text
Let’s say that we need to draw a **rounded** corner background on text, supporting the following cases:

* Set the background on one line text
![Text on one line](https://miro.medium.com/max/1155/0*PWKx5cM1bjVjA1aW "Text on one line with a rounded corner background")
* Set the background on text over **two or multiple lines**
![Text on multiple lines](https://miro.medium.com/max/1155/0*sdYBIZugLh_DJFvu "Text on multiple lines with a rounded corner background")
* Set the background on **right-to-left text**
![](https://miro.medium.com/max/1155/0*sdYBIZugLh_DJFvu "Right to left text with rounded corner background")
How can we implement this? Read on to find out, or jump directly to the [sample code](https://github.com/googlesamples/android-text/tree/master/RoundedBackground-Kotlin).
### To span or not to span? This is the question!
In [previous articles](https://medium.com/google-developers/underspanding-spans-1b91008b97e4) we’ve covered styling sections of text (even [internationalized text](https://medium.com/google-developers/styling-internationalized-text-in-android-f99759fb7b8f)). The solution involved using either framework or custom spans. While spans are a great solution in many cases, they do have some limitations that make them unsuitable for our problem. Appearance spans, like `BackgroundColorSpan`, give us access to the `TextPaint`, allowing us to change elements like the background color of text, but are only able to draw a solid color and can’t control elements like the corner radius.
![](https://miro.medium.com/max/507/0*jPTOmlEs5d6MLtuV "Text using a BackgroundColorSpan")
We need to draw a drawable together with the text. We can implement a custom `ReplacementSpan` to draw the background and the text ourselves. However `ReplacementSpans` cannot flow into the next line, therefore we will not be able to support a multi-line background. They would rather look like [Chip](https://material.io/design/components/chips.html) , the Material Design component, where every element must fit on a single line.
Spans work at the `TextPaint` level, not the layout level. Therefore, they can’t know about the line number the text starts and ends on, or about the paragraph direction (left-to-right or right-to-left)
### Solution: custom TextView
Depending on the position of the text, we need to draw four different drawables as text backgrounds:

* Text fits on one line: we only need one drawable
* Text fits on 2 lines: we need drawables for the start and end of the text
* Text spans multiple lines: we need drawables for the start, middle and end of the text.
![](https://miro.medium.com/max/507/0*jPTOmlEs5d6MLtuV "The four drawables that need to be drawn depending on the position of the text")
To position the background, we need to:

* Determine whether the text spans multiple lines 
* Find the start and end lines 
* Find the start and end offset depending on the paragraph direction 

All of these can be computed based on the text [Layout](https://developer.android.com/reference/android/text/Layout). To render the background behind the text we need access to the `Canvas`. A custom `TextView` has access to all of the information necessary to position the drawables and render them.
Our solution involves splitting our problem into 4 parts and creating classes dealing with them individually:

* **Marking the position of the background** is done in the XML resources with `Annotation` spans and then, in the code, we compute the positions in the `SearchBgHelper`
* Providing the background **drawables** as **attributes** of the TextView — implemented in `TextRoundedBgAttributeReader`
* **Rendering the drawables** depending on whether the text runs across **one or multiple lines** — `SearchBgRenderer` interface and its implementations: `SingleLineRenderer` and `MultiLineRenderer`
* Supporting **custom drawing** on a `TextView` — `RoundedBgTextView`, a class that extends `AppCompatTextView`, reads the attributes with the help of `TextRoundedBgAttributeReader`, overrides onDraw where it uses `SearchBgHelper` to draw the background.
### Finding out where the background should be drawn
We specify parts of the text that should have a background by using `Annotation` spans in our string resources. Find out more about working with Annotation spans from [this article](https://medium.com/google-developers/styling-internationalized-text-in-android-f99759fb7b8f).

We created a `SearchBgHelper` class that:

* Enables us to position the background based on the text directionality: left-to-right or right-to-left
* Renders the background, based on the drawables and the horizontal and vertical padding

In the `SearchBgHelper.draw` method, for every `Annotation` span found in the text, we get the start and end index of the span, find the line number for each and then compute the start and end character offset (within the line). Then, we use the `SearchBgRenderer` implementations to render the background.
```fun draw(canvas: Canvas, text: Spanned, layout: Layout) {
    // ideally the calculations here should be cached since 
    // they are not cheap. However, proper
    // invalidation of the cache is required whenever 
    // anything related to text has changed.
    val spans = text.getSpans(0, text.length, Annotation::class.java)
    spans.forEach { span ->
        if (span.value.equals("rounded")) {
            val spanStart = text.getSpanStart(span)
            val spanEnd = text.getSpanEnd(span)
            val startLine = layout.getLineForOffset(spanStart)
            val endLine = layout.getLineForOffset(spanEnd)

            // start can be on the left or on the right depending 
            // on the language direction.
            val startOffset = (layout.getPrimaryHorizontal(spanStart)
                + -1 * layout.getParagraphDirection(startLine) * horizontalPadding).toInt()
            // end can be on the left or on the right depending 
            // on the language direction.
            val endOffset = (layout.getPrimaryHorizontal(spanEnd)
                + layout.getParagraphDirection(endLine) * horizontalPadding).toInt()

            val renderer = if (startLine == endLine) singleLineRenderer else multiLineRenderer
            renderer.draw(canvas, layout, startLine, endLine, startOffset, endOffset)
        }
    }
}```
### Provide drawables as attributes
To easily supply drawables for different `TextViews` in our app, we define 4 custom attributes corresponding to the drawables and 2 attributes for the horizontal and vertical padding. We created a `TextRoundedBgAttributeReader` class that reads these attributes from the xml layout.
### Render the background drawable(s)
Once we have the drawables we need to draw them. For that, we need to know:

* The start and end line for the background
* The character offset where the background should start and end at.

We created an abstract class `SearchBgRenderer` that knows how to compute the top and the bottom offset of the line, but exposes an abstract `draw` function:
```abstract fun draw(canvas: Canvas,layout: Layout,startLine: Int,endLine: Int,startOffset: Int,endOffset: Int)```
The `draw` function will have different implementations depending on whether our text spans a single line or multiple lines. Both of the implementations work on the same principle: based on the line top and bottom, set the bounds of the drawable and render it on the canvas.

The single line implementation only needs to draw one drawable.
![Single line text with search rounded background](https://miro.medium.com/max/1155/0*HS6zOL8stqjTodpJ "Single line text")
The multi-line implementation needs to draw the start and the end of line drawables on the first, and last line respectively, then for each line in the middle, draw the middle line drawable.
![](https://miro.medium.com/max/1155/0*Tz3lRa59dixAI5LA "Multi-line text")
### Supporting custom drawing on a TextView
We extend `AppCompatTextView` and override `onDraw` to call SearchBgHelper.draw before letting the `TextView` draw the text.

**Caveat:** Our sample makes all the calculations for every `TextView.onDraw` method call. If you’re planning on integrating this implementation in your app, we strongly suggest to modify it and cache the calculations done in `SearchBgHelper`. Then, make sure you invalidate the cache whenever anything related to text, like text color, size or other properties, has changed.

***

The Android text APIs allow you a great deal of freedom to perform styling. Simple styling is possible with text attributes, styles, themes and spans. Complex custom styling is achievable by taking control of the `onDraw` method and making decisions on what and how to draw on the `Canvas`. Check out our sample for all implementation details.

What kind of text styling did you have to do that required custom `TextView` implementations? What kind of issues did you encounter? Tell us in the comments!
""".trimIndent()