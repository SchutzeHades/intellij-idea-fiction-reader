package schutze.hades.intellij.idea.fiction.reader.model

data class FictionData(
        val title: String,
        val content: String,
        val previousChapterUrl: String?,
        val nextChapterUrl: String?,
        val currentUrl: String
)
