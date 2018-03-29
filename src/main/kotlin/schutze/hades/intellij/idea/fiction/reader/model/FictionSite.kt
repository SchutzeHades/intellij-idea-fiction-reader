package schutze.hades.intellij.idea.fiction.reader.model

import schutze.hades.intellij.idea.fiction.reader.service.BaLingTxtFictionService
import schutze.hades.intellij.idea.fiction.reader.service.FictionService

enum class FictionSite(
        val displayName: String,
        val default: Boolean,
        val fictionService: FictionService
) {
    BALING_TXT("八零电子书", true, BaLingTxtFictionService)
}
