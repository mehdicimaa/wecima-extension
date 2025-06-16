
package com.wecima

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.api.*
import com.lagradost.cloudstream3.extractors.*
import org.jsoup.Jsoup

class WecimaPlugin : MainAPI() {
    override var mainUrl = "https://wecima.video"
    override var name = "Wecima"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val lang = "ar"

    // البحث في الموقع
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val doc = app.get(url).document
        return doc.select("div.post-box").map {
            val title = it.select("h2.post-title").text()
            val posterUrl = it.select("img").attr("src")
            val link = it.select("a").attr("href")
            MovieSearchResponse(
                title = title,
                href = link,
                quality = getQualityFromString(title),
                posterUrl = posterUrl,
                apiName = this.name
            )
        }
    }

    // تفاصيل الفيديو
    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.select("h1.post-title").text()
        val poster = doc.select("div.poster img").attr("src")
        val description = doc.select("div.story p").text()
        val episodes = doc.select("div.servers-list a").map {
            Episode(
                it.text(),
                it.attr("href")
            )
        }

        return MovieLoadResponse(
            name = title,
            url = url,
            apiName = this.name,
            type = TvType.Movie,
            posterUrl = poster,
            year = null,
            plot = description,
            recommendations = emptyList(),
            episodes = episodes
        )
    }

    // استخراج روابط الفيديو
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val videoFrame = doc.select("iframe").attr("src")
        if (videoFrame.isNotEmpty()) {
            callback(
                ExtractorLink(
                    name = "Wecima",
                    source = "wecima.video",
                    url = videoFrame,
                    quality = Qualities.Unknown.value,
                    isM3u8 = false
                )
            )
            return true
        }
        return false
    }
}
