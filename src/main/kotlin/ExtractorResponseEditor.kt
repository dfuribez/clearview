package com.example

import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.MimeType
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor
import java.awt.Component

class ExtractorResponseEditor(
  var montoyaApi: MontoyaApi,
  var editorCreationContext: EditorCreationContext
) : ExtensionProvidedHttpResponseEditor {

  private var currentResponse: HttpRequestResponse? = null

  override fun getResponse(): HttpResponse? {
    return this.currentResponse?.response()
  }

  override fun setRequestResponse(requestResponse: HttpRequestResponse) {
    currentResponse = requestResponse
  }

  override fun isEnabledFor(requestResponse: HttpRequestResponse?): Boolean {
    if (requestResponse == null) { return false }
    return requestResponse.response().inferredMimeType().equals(MimeType.HTML)
  }

  override fun caption(): String {
    return "Extract"
  }

  override fun uiComponent(): Component {
    val tabGui = ExtractorResponseTabGUI(montoyaApi, currentResponse)
    return tabGui.getMainPanel()
  }

  override fun selectedData(): Selection? {
    return null
  }

  override fun isModified(): Boolean {
    return false
  }
}
