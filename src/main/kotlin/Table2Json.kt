@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


class Table2Json {
  fun table2JSON(name: String, table: Element) : String {
    var headers: List<String>? = null
    var values: List<String>? = null

    val classes = table.classNames().joinToString(".")
    val id = table.id()

    val separator = if (id.isNotEmpty()) " - " else ""
    val tableName = "${id}${separator}${classes}"

    val theaders = table.select("thead")
    val body = table.select("tbody")

    var content: Elements

    if (!theaders.isEmpty()) {
      headers = parseTr(theaders.select("tr").first())
    } else {
      headers = parseTr(table.select("tr").first())
    }

    if (!body.isEmpty()) {
      content = body.select("tr")
    } else {
      content = table.select("tr")
    }

    val v = mutableListOf<JsonObject>()

    for (tr in content) {
      v.add(generateJSONNode(headers, parseTr(tr)))
    }

    val json = buildJsonObject {
      putJsonArray(tableName) {
        for (a in v) add(a)
      }
    }

    val format = Json {
      prettyPrint = true
      prettyPrintIndent = " "
    }
    return format.encodeToString(JsonObject.serializer(), json)
  }

  fun generateJSONNode(headers: List<String>?, values: List<String>): JsonObject {
    val json = buildJsonObject {
      for ((index, value) in values.withIndex()) {
        var header: String = ""
        if (headers == null || index >= headers.size) {
          header = "element $index"
        } else {
          header = headers[index]
        }
        put(header, JsonPrimitive(value))
      }
    }

    return json
  }

  fun parseTr(tr: Element?): List<String> {
    val content = mutableListOf<String>()

    if (tr == null) return content

    for (element in tr.select("th, td")) {
      if (element.tagName().equals("th") ||
        element.tagName().equals("td")) {
        content.add(element.text())
      }
    }
    return content
  }
}