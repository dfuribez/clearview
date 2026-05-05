package com.example.customui

import net.miginfocom.swing.MigLayout
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class CollapsiblePanel (
  var title: String,
  content: JComponent,
  checks: JComponent
) : JPanel() {

  private val button = JLabel("▼ $title")
  private val mainPanel = JPanel(MigLayout())

  private var isExpanded = true

  private val mainConstrains = "grow, push, span"

  init {
    layout = MigLayout("insets 0")
    mainPanel.add(content, "grow, push")

    add(button, "growx, pushx")
    add(checks, "wrap")
    add(mainPanel, mainConstrains)

    button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

    button.addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent) {
        toggle()
      }
    })
  }

  private fun toggle() {
    isExpanded = !isExpanded

    if (isExpanded) {
      add(mainPanel, mainConstrains)
      button.text = "▼ $title"
    } else {
      button.text = "▶ $title"
      remove(mainPanel)
    }

    revalidate()
    repaint()
  }

}