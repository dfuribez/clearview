package com.example

import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import org.fife.ui.rtextarea.SearchContext
import org.fife.ui.rtextarea.SearchEngine
import org.jsoup.Jsoup
import java.io.IOException
import javax.swing.*


class ExtractorResponseTabGUI(
  val montoyaApi: MontoyaApi,
  val response: HttpRequestResponse?) {
  private val mainPanel = JPanel(MigLayout())

  private val selectorTextField = JTextField()
  private val searchTextField = JTextField()

  private val removeTagsCheckBox = JCheckBox("Remove Tags")
  private val removeJSCheckBox = JCheckBox("Remove Scrips")
  private val convertJSONCheckBox = JCheckBox("Convert to JSON")

  private val enterButton = JButton("->")
  private val previousButton = JButton("<")
  private val nextButton = JButton(">")

  private val colourText = RSyntaxTextArea(20, 20)

  private val scrollHTML = RTextScrollPane(colourText)

  private var originalBody: String? = null


  init {
    originalBody = response?.response()?.bodyToString()

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

    colourText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML)
    generateLayout()
    addActions()
  }

  private fun addActions() {
    selectorTextField.addActionListener { enterHandler() }
    searchTextField.addActionListener { search() }

    enterButton.addActionListener { enterHandler() }
    previousButton.addActionListener { search(false) }
    nextButton.addActionListener { search() }
  }

  private fun generateLayout() {
    mainPanel.add(selectorTextField, "growx, pushx")
    mainPanel.add(enterButton, "wrap")

    // Options
    val checksPanel = JPanel(MigLayout("insets 0"))
    checksPanel.add(removeTagsCheckBox)
    checksPanel.add(removeJSCheckBox)
    checksPanel.add(convertJSONCheckBox)

    mainPanel.add(checksPanel, "span, growx, wrap")
    mainPanel.add(scrollHTML, "span, grow, push, wrap")

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
    val text = searchTextField.text

    if (text.isEmpty()) return

    val context = SearchContext()
    context.searchFor = text
    context.searchForward = forward

    val found = SearchEngine.find(colourText, context).wasFound()

  }

  private fun enterHandler() {
    val selector = selectorTextField.text
    val removeTags = removeTagsCheckBox.isSelected
    Thread {
      val result = parseHTML(selector, removeTags)
      SwingUtilities.invokeLater {
        colourText.text = result
      }
    }.start()
  }

  private fun parseHTML(
    selector: String,
    removeTags: Boolean): String {
    if (originalBody == null) { return "" }

    val document = Jsoup.parse(originalBody)
    val elements = document.select(selector)

    val result = StringBuilder()

    if (selector.equals("table", ignoreCase = true)) {
      val conversor = Table2Json()
      colourText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON)

      for (table in elements) {
        result.append(conversor.table2JSON("table", table))
      }

      return result.toString()
    }

    for (li in elements) {
      if (removeTags) {
        result.append(li.text().trim())
      } else {
        result.append(li.html().trim())
      }

      result.append(System.lineSeparator())
    }
    return result.toString()
  }

  fun getMainPanel(): JPanel {
    return mainPanel
  }

}

