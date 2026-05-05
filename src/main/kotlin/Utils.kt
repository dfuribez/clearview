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
