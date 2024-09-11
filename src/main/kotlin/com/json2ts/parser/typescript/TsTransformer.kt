package com.json2ts.parser.typescript

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class TsTransformer
    (private val jsonString: String, private val rootName: String, private val tsParseType: ParseType) :
    TsPrimitiveConverter(), ToCode {
    private val typeMap = mutableMapOf(rootName to "")

    init {
        create()
    }

    override fun toCode(): String {
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
                typeMap[keyName] = "export type $keyName = $type;"
            }

            jsonElement.isJsonNull -> {
                type = traverseNull(jsonElement.asJsonNull, key)
                typeMap[keyName] = "export type $keyName = $type;"
            }

            jsonElement.isJsonPrimitive -> {
                type = traversePrimitive(jsonElement.asJsonPrimitive, key)
                typeMap[keyName] = "export type $keyName = $type;"
            }
        }
    }

    override fun traverseSingleObject(jsonObject: JsonObject, key: String?): String {
        val typeName = toCamelcase(key ?: rootName)
        val set = jsonObject.entrySet()
        var code = ""

        set.forEachIndexed { index, entry ->
            val (entryKey, type) = getKeyAndType(entry, key)
            val value = entry.value
            val isClass = tsParseType == ParseType.TSClass
            val prefix = if (isClass) "private " else ""
            code += if (value.isJsonNull) {
                "\t${prefix}$entryKey?: $type;"
            } else {
                "\t${prefix}$entryKey: $type;"
            }

            if (index < set.size - 1) {
                code += "\n"
            }
        }
        if (tsParseType == ParseType.TSClass) {
            set.forEachIndexed { index, entry ->
                val (entryKey, type) = getKeyAndType(entry, key)
                // make filed uppercase first char and do not use replaceFirstChar api
                val fistChar = entryKey[0].toUpperCase()
                val uppercaseFirstEntry = fistChar + entryKey.substring(1)
                code +=
                    "\n\tpublic set${uppercaseFirstEntry} ($entryKey: $type) {\n\t\tthis.${entryKey} = $entryKey;\n\t}\n\tpublic get${uppercaseFirstEntry} () {\n\t\treturn this.$entryKey;\n\t}"
            }

        }
        when (tsParseType) {
            ParseType.InterfaceStruct -> {
                typeMap[typeName] =
                    "export interface $typeName {\n$code\n}"
            }

            ParseType.TypeStruct -> {
                typeMap[typeName] =
                    "export type $typeName = {\n$code\n}"
            }

            ParseType.TSClass -> {
                typeMap[typeName] =
                    "export class $typeName {\n$code\n}"
            }

            else -> {
                typeMap[typeName] =
                    "export interface $typeName {\n$code\n}"
            }
        }
        return typeName
    }

    private fun getKeyAndType(
        entry: MutableMap.MutableEntry<String, JsonElement>,
        key: String?
    ): Pair<String, String> {
        var entryKey = entry.key
        if (entryKey.indexOf(" ") >= 0) {
            entryKey = """"$entryKey""""
        }
        val value = entry.value
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
        return Pair(entryKey, type)
    }
}
