package com.json2ts.parser

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

open class JsDocConverter(private val jsonString: String, private val rootName: String) : TsPrimitiveConverter() {
    private val typeMap = mutableMapOf(rootName to "")

    init {
        create()
    }

    fun toCode(): String {
        return typeMap.values.joinToString("\n")
    }

    private lateinit var rootJsonElement: JsonElement

    private fun create() {
        rootJsonElement = JsonParser.parseString(jsonString)
        traverseRoot(rootJsonElement, rootName)
    }

    private fun traverseRoot(jsonElement: JsonElement, key: String?) {
        val keyName = key ?: rootName
        val type: String
        when {
            jsonElement.isJsonObject && !jsonElement.isJsonArray -> {
                traverseSingleObject(jsonElement.asJsonObject, key)
            }
            jsonElement.isJsonArray -> {
                type = traverseArray(jsonElement.asJsonArray, "${key}Child")

                typeMap[keyName] = """
                    /**
                    *@typedef $keyName
                    *@type {$type}
                    */
                """.trimIndent()
            }
            jsonElement.isJsonNull -> {
                type = traverseNull(jsonElement.asJsonNull, key)
                typeMap[keyName] = """
                    /**
                    *@typedef $keyName
                    *@type {$type}
                    */
                """.trimIndent()
            }
            jsonElement.isJsonPrimitive -> {
                type = traversePrimitive(jsonElement.asJsonPrimitive, key)
                typeMap[keyName] = """
                    /**
                    *@typedef $keyName
                    *@type {$type}
                    */
                """.trimIndent()
            }
        }
    }

    override fun traverseSingleObject(jsonObject: JsonObject, key: String?): String {
        var doc = "/**\n"
        val typeName = toCamelcase(key ?: rootName)
        doc += "*@typedef $typeName\n"
        val set = jsonObject.entrySet()
        for (entry in set) {
            val value = entry.value
            val entryKey = entry.key
            val camelCaseKey = toCamelcase(key ?: "", entryKey)
            // default type is any
            var type = "any"
            when {
                value.isJsonPrimitive -> {
                    type = traversePrimitive(value.asJsonPrimitive, camelCaseKey)
                }
                value.isJsonObject -> {
                    type = traverseSingleObject(value.asJsonObject, camelCaseKey)
                }
                value.isJsonArray -> {
                    type = traverseArray(value.asJsonArray, camelCaseKey)
                }
                value.isJsonNull -> {
                    type = traverseNull(value.asJsonNull, camelCaseKey)
                }
            }
            doc += if (value.isJsonNull) {
                "*@property {$type} [$entryKey]\n"
            } else {
                "*@property {$type} $entryKey\n"
            }
        }
        doc += "*/"
        typeMap[typeName] = doc
        return typeName
    }
}
