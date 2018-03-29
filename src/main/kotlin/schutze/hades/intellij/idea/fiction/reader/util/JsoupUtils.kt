package schutze.hades.intellij.idea.fiction.reader.util

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

fun getJsoupConnection(url: String): Connection = Jsoup.connect(url) //
        .userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36") //
        .timeout(TimeUnit.MINUTES.toMillis(10L).toInt()) //
        .ignoreContentType(true)
