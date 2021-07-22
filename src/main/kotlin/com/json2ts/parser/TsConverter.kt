package icons.com.json2ts.parser

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.json2ts.parser.ParseType
import com.json2ts.parser.TsPrimitiveConverter

class TsConverter
(private val jsonString: String, private val rootName: String, private val tsParseType: ParseType) :
    TsPrimitiveConverter() {
    private val typeMap = mutableMapOf(rootName to "")

    init {
        create()
    }

    fun toCode(): String {
        return typeMap.values.joinToString("\n")
    }

    private lateinit var rootJsonElement: JsonElement

    private fun create() {
        rootJsonElement = JsonParser().parse(jsonString)
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
            val entryKey = entry.key
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
            code += if (value.isJsonNull) {
                "\t$entryKey?: $type;"
            } else {
                "\t$entryKey: $type;"
            }
            if (index < set.size - 1) {
                code += "\n"
            }
        }
        if (tsParseType == ParseType.InterfaceStruct) {
            typeMap[typeName] = """export interface $typeName {
$code
}
            """.trimIndent()
        } else {
            typeMap[typeName] = """export type $typeName = {
$code
}
            """.trimIndent()
        }
        return typeName
    }
}
