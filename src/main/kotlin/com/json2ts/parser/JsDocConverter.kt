package com.json2ts.parser

import com.google.gson.*

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
        rootJsonElement = try {
            JsonParser.parseString(jsonString)
        } catch (e: Exception) {
            throw e
        }
        traverseRoot(rootJsonElement, null, rootName)
    }

    private fun traverseRoot(jsonElement: JsonElement, parentJsonElement: JsonElement?, key: String?) {
        val keyName = key ?: rootName
        val type: String
        when {
            jsonElement.isJsonObject && !jsonElement.isJsonArray -> {
                traverseSingleObject(jsonElement.asJsonObject, parentJsonElement, key)
            }
            jsonElement.isJsonArray -> {
                type = traverseArray(jsonElement.asJsonArray, parentJsonElement, "${key}Child")

                typeMap[keyName] = """
                    /**
                    *@typedef $keyName
                    *@type {$type}
                    */
                """.trimIndent()
            }
            jsonElement.isJsonNull -> {
                type = traverseNull(jsonElement.asJsonNull, parentJsonElement, key)
                typeMap[keyName] = """
                    /**
                    *@typedef $keyName
                    *@type {$type}
                    */
                """.trimIndent()
            }
            jsonElement.isJsonPrimitive -> {
                type = traversePrimitive(jsonElement.asJsonPrimitive, parentJsonElement, key)
                typeMap[keyName] = """
                    /**
                    *@typedef $keyName
                    *@type {$type}
                    */
                """.trimIndent()
            }
        }
    }

    override fun traverseSingleObject(jsonObject: JsonObject, parentJsonElement: JsonElement?, key: String?): String {
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
                    type = traversePrimitive(value.asJsonPrimitive, jsonObject, camelCaseKey)
                }
                value.isJsonObject -> {
                    type = traverseSingleObject(value.asJsonObject, jsonObject, camelCaseKey)
                }
                value.isJsonArray -> {
                    type = traverseArray(value.asJsonArray, jsonObject, camelCaseKey)
                }
                value.isJsonNull -> {
                    type = traverseNull(value.asJsonNull, jsonObject, camelCaseKey)
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