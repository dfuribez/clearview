package com.example

import org.jsoup.select.QueryParser
import java.awt.Component
import javax.swing.JDialog
import javax.swing.JOptionPane

fun checkSelector(selector: String) : Boolean {
  try {
    QueryParser.parse(selector)
    return true
  } catch (e: Exception) {
    return false
  }
}

fun showMessage(parent: Component, message: String) {
    JOptionPane.showMessageDialog(parent, message)
}

fun isValidRegex(pattern: String) : Boolean{
  try {
    Regex(pattern)
    return true
  } catch (e: Exception) {
    return false
  }
}

fun manualWrap(s: String, limit: Int): String {
  val sb = StringBuilder()

  for (line in s.split(System.lineSeparator())) {
    var i = 0
    while (i < line.length) {
      val end = minOf(i + limit, line.length)
      sb.append(line.substring(i, end))
      sb.append(System.lineSeparator())
      i += limit
    }
  }

  return sb.toString()
}