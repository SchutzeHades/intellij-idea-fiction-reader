package schutze.hades.intellij.idea.fiction.reader

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory.SERVICE
import schutze.hades.intellij.idea.fiction.reader.model.FictionData
import schutze.hades.intellij.idea.fiction.reader.model.FictionSite
import schutze.hades.intellij.idea.fiction.reader.service.BaLingTxtFictionService
import schutze.hades.intellij.idea.fiction.reader.service.FictionService
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField

class ReaderToolWindowFactory : ToolWindowFactory {

    private lateinit var readerPanel: JPanel

    private lateinit var fictionContent: JTextArea

    private lateinit var customUrl: JTextField

    private lateinit var readBtn: JButton

    private lateinit var previousChapterBtn: JButton

    private lateinit var nextChapterBtn: JButton

    private lateinit var siteComboBox: JComboBox<String>

    private var fictionService: FictionService = BaLingTxtFictionService

    private var fictionMetaData = FictionMetaData()

    private val allButtons = listOf(
            readBtn,
            previousChapterBtn,
            nextChapterBtn
    )

    init {
        readBtn.addActionListener(object : ChangeChapterActionListener() {
            override fun getUrl(): String? {
                return customUrl.text
            }

            override fun getButton(): JButton {
                return readBtn
            }

        })
        previousChapterBtn.addActionListener(object : ChangeChapterActionListener() {
            override fun getUrl(): String? {
                return fictionMetaData.previousChapterUrl
            }

            override fun getButton(): JButton {
                return previousChapterBtn
            }
        })
        nextChapterBtn.addActionListener(object : ChangeChapterActionListener() {
            override fun getUrl(): String? {
                return fictionMetaData.nextChapterUrl
            }

            override fun getButton(): JButton {
                return nextChapterBtn
            }
        })
        siteComboBox.addActionListener {
            refreshFictionService()
        }
        FictionSite.values().forEach { siteComboBox.addItem(it.displayName) }
    }

    private fun refreshFictionService() {
        val selectedSite = FictionSite.values().filter { it.ordinal == siteComboBox.selectedIndex }.takeIf { it.isNotEmpty() }?.single()
                ?: FictionSite.values().single { it.default }
        if (siteComboBox.selectedIndex != selectedSite.ordinal) {
            siteComboBox.selectedIndex = selectedSite.ordinal
        }
        fictionService = selectedSite.fictionService
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        refreshFictionService()
        toolWindow.contentManager.addContent(SERVICE.getInstance().createContent(readerPanel, "", false))
    }

    private inner class FictionRenderThread(
            private val url: String,
            private val button: JButton,
            private val originalBtnText: String
    ) : Runnable {

        override fun run() {
            renderFiction(url)
            button.text = originalBtnText
            readBtn.isEnabled = true
            if (fictionMetaData.previousChapterUrl?.isNotEmpty() == true) {
                previousChapterBtn.isEnabled = true
            }
            if (fictionMetaData.nextChapterUrl?.isNotEmpty() == true) {
                nextChapterBtn.isEnabled = true
            }
        }

        private fun renderFiction(url: String) {
            val fictionData: FictionData
            try {
                fictionData = fictionService.queryByUrl(url)
            } catch (e: Exception) {
                val stackTraceWriter = StringWriter()
                e.printStackTrace(PrintWriter(stackTraceWriter))
                fictionContent.text = "访问出现异常: url = $url\n$stackTraceWriter"
                return
            }
            fictionMetaData = FictionMetaData(fictionData.previousChapterUrl, fictionData.nextChapterUrl)
            val fictionContentText = "${fictionData.title}\n${fictionData.content}"
            if (fictionContentText.isNotBlank()) {
                fictionContent.text = fictionContentText
                customUrl.text = url
            } else {
                fictionContent.text = "解析失败，请检查本次请求的地址是否正确：${fictionData.currentUrl}"
                customUrl.text = ""
            }
            fictionContent.moveCaretPosition(0)
        }
    }

    private data class FictionMetaData(
            val previousChapterUrl: String? = null,
            val nextChapterUrl: String? = null
    )

    private abstract inner class ChangeChapterActionListener : ActionListener {

        override fun actionPerformed(e: ActionEvent?) {
            var url = getUrl()
            if (url?.isNotBlank() != true) {
                return
            }
            url = url.trim()
            if (!url.startsWith('/')) {
                url = "/$url"
            }
            val button = getButton()
            allButtons.forEach { it.isEnabled = false }
            val originalBtnText = button.text
            button.text = "加载中..."
            Thread(FictionRenderThread(url, button, originalBtnText)).start()
        }

        protected abstract fun getUrl(): String?

        protected abstract fun getButton(): JButton
    }
}
