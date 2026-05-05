package com.example

import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import com.example.customui.CollapsiblePanel
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import org.fife.ui.rtextarea.SearchContext
import org.fife.ui.rtextarea.SearchEngine
import org.jsoup.Jsoup
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.swing.*


class ExtractorResponseTabGUI(
  val montoyaApi: MontoyaApi
) {
  private val mainPanel = JPanel(MigLayout())

  private val selectorTextField = JTextField()
  private val searchTextField = JTextField()

  private val removeSelectorText = JTextField()
  private val replaceText = JTextField()
  private val withText = JTextField()

  private val removeTagsCheckBox = JCheckBox("Remove Tags")
  private val removeHeadCheckBox = JCheckBox("Remove Head")
  private val wrapCheckBox = JCheckBox("Wrap")

  private val enterButton = JButton("Search")
  private val previousButton = JButton("<")
  private val nextButton = JButton(">")
  private val removeSelectorButton = JButton("Remove")
  private val replaceButton = JButton("Replace")

  private val progressBar = JProgressBar()

  private val colourText = RSyntaxTextArea(20, 20)

  private val scrollHtml = RTextScrollPane(colourText)

  private var originalBody: String? = null

  private var response: HttpRequestResponse? = null

  private var working: Boolean = false

  init {
    try {
      val theme: Theme = Theme.load(
        javaClass.getResourceAsStream(
          "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"
        )
      )
      theme.apply(colourText)
    } catch (ioe: IOException) {
      ioe.printStackTrace()
    }

    scrollHtml.lineNumbersEnabled = false

    colourText.setFont(Font("Monospaced", Font.PLAIN, 12));
    colourText.lineWrap = false
    colourText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML)

    searchTextField.text = Constants.TAB.searchPlaceHolder
    selectorTextField.text = Constants.TAB.selectorPlaceHolder

    searchTextField.foreground = Color.GRAY
    selectorTextField.foreground = Color.GRAY

    wrapCheckBox.isSelected = true

    generateLayout()
    addActions()
  }

  fun wrap(s: String): String {
    val width = scrollHtml.width
    val fm: FontMetrics = colourText.getFontMetrics(colourText.getFont())
    val charWidth = fm.charWidth('m')
    val maxCharsPerLine: Int = width / charWidth

    return manualWrap(s, maxCharsPerLine - 3)
  }

  fun modifiedResponse(response: HttpRequestResponse?) {
    val bodyBytes = response?.response()?.body()?.bytes ?: return
    originalBody = String(bodyBytes, StandardCharsets.UTF_8)
    this.response = response

    enterHandler()
    SwingUtilities.invokeLater {
      SwingUtilities.invokeLater {
        colourText.caretPosition = 0
        colourText.requestFocusInWindow()
        search()
      }
    }
  }

  private fun addActions() {
    selectorTextField.addActionListener { enterHandler() }
    searchTextField.addActionListener { search() }

    enterButton.addActionListener { enterHandler() }
    previousButton.addActionListener { search(false) }
    nextButton.addActionListener { search() }

    removeTagsCheckBox.addActionListener { enterHandler() }
    removeHeadCheckBox.addActionListener { enterHandler() }
    removeSelectorText.addActionListener { enterHandler() }
    removeSelectorButton.addActionListener { enterHandler() }
    replaceText.addActionListener { enterHandler() }
    withText.addActionListener { enterHandler() }
    replaceButton.addActionListener { enterHandler() }

    wrapCheckBox.addActionListener { enterHandler() }

    selectorTextField.onFocusChange(
      gained = {removePlaceHolder(selectorTextField, Constants.TAB.selectorPlaceHolder)},
      lost = { addPlaceHolder(selectorTextField, Constants.TAB.selectorPlaceHolder) }
    )

    searchTextField.onFocusChange(
      gained = { removePlaceHolder(searchTextField, Constants.TAB.searchPlaceHolder) },
      lost = { addPlaceHolder(searchTextField, Constants.TAB.searchPlaceHolder) }
    )
  }

  private fun generateLayout() {
    mainPanel.add(progressBar, "north, height 2!, wrap")
    mainPanel.add(selectorTextField, "growx, pushx")
    mainPanel.add(enterButton, "wrap")

    // Options
    val checksPanel = JPanel(MigLayout("insets 0"))
    checksPanel.add(removeTagsCheckBox)
    checksPanel.add(removeHeadCheckBox)
    checksPanel.add(wrapCheckBox)

    // Cillapsable
    val panel = JPanel(MigLayout("insets 0"))

    panel.add(JLabel("Remove:"))
    panel.add(removeSelectorText, "growx, pushx, span 3")
    panel.add(removeSelectorButton, "wrap")

    panel.add(JLabel("Replace:"))
    panel.add(replaceText, "growx, pushx")
    panel.add(JLabel("With:"))
    panel.add(withText, "growx, pushx")
    panel.add(replaceButton)

    val collapsible = CollapsiblePanel("", panel, checksPanel)

    mainPanel.add(collapsible, "span, growx, wrap")

    mainPanel.add(scrollHtml, "span, grow, push, wrap")

    // Search
    val searchPanel = JPanel(MigLayout("insets 0"))
    searchPanel.add(searchTextField, "pushx, growx")
    searchPanel.add(previousButton)
    searchPanel.add(nextButton)

    mainPanel.add(searchPanel, "span, growx")
    SwingUtilities.invokeLater(Runnable {
      selectorTextField.requestFocusInWindow()
    })
  }

  private fun search(forward: Boolean = true) {
    progressBar.isIndeterminate = true
    progressBar.isVisible = true
    object : SwingWorker<Boolean, Unit>() {
      override fun doInBackground(): Boolean {
        val text = searchTextField.text
        if (text.isEmpty()) return true

        val context = SearchContext()
        context.searchFor = text
        context.matchCase = false
        context.isRegularExpression = false
        context.searchForward = forward
        context.searchWrap = true

        val caret = colourText.caretPosition
        if (caret < colourText.document.length) colourText.caretPosition = caret + 1

        SearchEngine.find(colourText, context).wasFound()
        return true
      }

      override fun done() {
        val result = get()
        progressBar.isIndeterminate = false
        progressBar.isVisible = false
      }
    }.execute()

  }

  private fun enterHandler() {
    progressBar.isIndeterminate = true
    progressBar.isVisible = true
    object : SwingWorker<String, Unit>() {
      override fun doInBackground(): String? {
        return process()
      }

      override fun done() {
        val result = get()
        colourText.text = result
        colourText.caretPosition = 0
        progressBar.isIndeterminate = false
        progressBar.isVisible = false
      }
    }.execute()
  }

  private fun process() : String? {

    val selector = selectorTextField.text
    val removeTags = removeTagsCheckBox.isSelected

    if (selector == "<selector>") { working = false; return null}

    val head = if (removeHeadCheckBox.isSelected) ",head" else ""
    var toRemove = removeSelectorText.text.trim() + head

    if (toRemove.startsWith(",head")) toRemove = "head"
    val p = montoyaApi.userInterface().swingUtils().suiteFrame()
    if (!checkSelector(selector) || (!checkSelector(toRemove) && toRemove.isNotBlank())) {
      showMessage(p, "Invalid CSS selector $selector or $toRemove")
      return null
    }
    val replace = replaceText.text
    val replaceWithText = withText.text

    try {
      var result = parseHtml(selector, removeTags, toRemove)

      if (replace.isNotEmpty()) {
        colourText.caretPosition = 0

        if (!isValidRegex(replace)) {
          showMessage(p, "Invalid regex: $replace")
          return null
        }

        result = result.replace(Regex(replace), replaceWithText)
      }

      if (wrapCheckBox.isSelected) result = wrap(result)
      return result
    } catch (e : Exception) {
      montoyaApi.logging().logToError(e.toString())
      return null
    }
  }

  private fun parseHtml(
    selector: String,
    removeTags: Boolean,
    toRemove: String
  ): String {
    if (originalBody == null) { return "" }

    val result = StringBuilder()
    val sel = if (selector.isEmpty()) "*" else selector

    val document = Jsoup.parse(originalBody!!)

    if (toRemove.isNotEmpty()) document.select(toRemove).remove()

    val elements = document.select(sel)

    if (selector.equals("table", ignoreCase = true)) {
      val conversor = Table2Json()
      colourText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON)

      for (table in elements) {
        result.append(conversor.table2JSON("table", table))
        result.append(System.lineSeparator())
      }
      return result.toString()
    }

    colourText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML)
    for (li in elements) {
      if (removeTags) {
        result.append(li.text().trim())
      } else {
        result.append(li.toString())
      }
      result.append(System.lineSeparator())
    }
    return result.toString()
  }

  fun getMainPanel(): JPanel {
    return mainPanel
  }

  private fun removePlaceHolder(text: JTextField, holder: String) {
    if (text.text.equals(holder)) text.text = ""
    text.foreground = Color.WHITE
  }

  private fun addPlaceHolder(text: JTextField, holder: String) {
    if (text.text.isEmpty() || text.text.equals(holder)) {
      text.text = holder
      text.foreground = Color.GRAY
      return
    }
    text.foreground = Color.WHITE
  }

  fun JTextField.onFocusChange(
    gained: () -> Unit = {},
    lost: () -> Unit = {}
  ) {
    addFocusListener(object : FocusListener {
      override fun focusGained(e: FocusEvent) = gained()
      override fun focusLost(e: FocusEvent) = lost()
    })
  }
}
