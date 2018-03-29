package schutze.hades.intellij.idea.fiction.reader.service

import schutze.hades.intellij.idea.fiction.reader.model.FictionData
import schutze.hades.intellij.idea.fiction.reader.util.getJsoupConnection

interface FictionService {

    fun queryByUrl(url: String): FictionData
}

object BaLingTxtFictionService : FictionService {

    private const val DOMAIN = "http://m.80txt.com"

    override fun queryByUrl(url: String): FictionData {
        println("载入中")
        val completeUrl = "$DOMAIN$url"
        val document = getJsoupConnection(completeUrl).get()
        println(completeUrl)
        return FictionData(
                title = document.select("h1#_bqgmb_h1").text(),
                content = document.select("div#nr1").html().replace("<br/ >", "\n").replace("<br>", "").replace("&nbsp;", " "),
                previousChapterUrl = document.select("a#pb_prev").attr("href").takeIf { isValidChapterUrl(it) },
                nextChapterUrl = document.select("a#pb_next").attr("href").takeIf { isValidChapterUrl(it) },
                currentUrl = completeUrl
        )
    }

    private fun isValidChapterUrl(url: String) = url.endsWith(".html") && url.split('/').filter { it.isNotEmpty() }.size >= 2
}
