package icons.com.json2ts.parser

import com.google.gson.*
import com.json2ts.parser.ParseType
import com.json2ts.parser.TsPrimitiveConverter

class TsConverter(private val jsonString: String, private val rootName: String, private val tsParseType: ParseType):TsPrimitiveConverter() {
    private val typeMap = mutableMapOf(rootName to "")
    init {
        create()
    }
    fun toCode ():String {
        return typeMap.values.joinToString("\n")
    }
    private lateinit var rootJsonElement: JsonElement

    private fun create () {
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
                typeMap[keyName] = "export type $keyName = $type;"
            }
            jsonElement.isJsonNull -> {
                type = traverseNull(jsonElement.asJsonNull, parentJsonElement, key)
                typeMap[keyName] = "export type $keyName = $type;"
            }
            jsonElement.isJsonPrimitive -> {
                type = traversePrimitive(jsonElement.asJsonPrimitive, parentJsonElement, key)
                typeMap[keyName] = "export type $keyName = $type;"
            }
        }
    }

    override fun traverseSingleObject(jsonObject: JsonObject, parentJsonElement: JsonElement?, key: String?): String {
        val typeName = toCamelcase(key ?: rootName)
        val set = jsonObject.entrySet()
        var code = ""

        set.forEachIndexed{ index,entry ->
            val entryKey = entry.key
            val value = entry.value
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
            typeMap[typeName] ="""export interface $typeName {
$code
}""".trimIndent()

        } else {
            typeMap[typeName] =  """export type $typeName = {
$code
}""".trimIndent()
        }
        return typeName
    }



}