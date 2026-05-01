package com.example

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor
import burp.api.montoya.ui.editor.extension.HttpResponseEditorProvider

class Extension : BurpExtension, HttpResponseEditorProvider {
  lateinit var montoyaApi: MontoyaApi

  override fun initialize(montoyaApi: MontoyaApi) {
    this.montoyaApi = montoyaApi
    montoyaApi.extension().setName("My Extension")
    montoyaApi.logging().logToOutput("Hi")

    montoyaApi.userInterface().registerHttpResponseEditorProvider(this)
  }

  override fun provideHttpResponseEditor(creationContext: EditorCreationContext): ExtensionProvidedHttpResponseEditor? {
    return ExtractorResponseEditor(montoyaApi, creationContext)
  }
}
