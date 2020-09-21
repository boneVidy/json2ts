package parser

import com.google.gson.*

class JsDocParser(jsonString: String,private val rootName: String) : JsonTravel(jsonString) {
    private val docsMap = mutableMapOf(rootName to "")
    init {
        create()
    }
    fun toRawStringDoc ():String {
       return docsMap.values.joinToString("\n")
    }
    private lateinit var rootJsonElement: JsonElement

    private fun create () {
        rootJsonElement = try {
            JsonParser().parse(jsonString)
        } catch (e: Exception) {
            throw e
        }
        travelRoot(rootJsonElement, null, rootName)
    }
    private fun travelRoot (jsonElement: JsonElement, parentJsonElement: JsonElement?, key: String?) {
        val keyName = key?:rootName
        var type = "any"
        when {
            jsonElement.isJsonObject && !jsonElement.isJsonArray -> {
                travelSingleObject(jsonElement.asJsonObject, parentJsonElement, key)
            }
            jsonElement.isJsonArray -> {
                type = travelArray(jsonElement.asJsonArray, parentJsonElement, "${key}Child" )

                docsMap[keyName] = """
                    /**
                    *@typedef {$keyName}
                    *@type {$type}
                    */
                """.trimIndent()
            }
            jsonElement.isJsonNull -> {
                type = travelNull(jsonElement.asJsonNull, parentJsonElement, key)
                docsMap[keyName] = """
                    /**
                    *@typedef {$keyName}
                    *@type {$type}
                    */
                """.trimIndent()
            }
            jsonElement.isJsonPrimitive -> {
                type = travelPrimitive(jsonElement.asJsonPrimitive, parentJsonElement, key)
                docsMap[keyName] = """
                    /**
                    *@typedef {$keyName}
                    *@type {$type}
                    */
                """.trimIndent()
            }
        }
    }

    override fun travelPrimitive(asJsonPrimitive: JsonPrimitive, parentJsonElement: JsonElement?, key: String?):String {
        var type = ""
        when {
            asJsonPrimitive.isString -> {
                type = "string"
            }
            asJsonPrimitive.isNumber -> {
                type = "number"
            }
            asJsonPrimitive.isBoolean -> {
                type = "boolean"
            }
        }
        return type
    }

    override fun travelNull(jsonNull: JsonNull, parentJsonElement: JsonElement?, key: String?):String {
        return "any"
    }

    override fun travelArray(jsonArray: JsonArray, parentJsonElement: JsonElement?, key: String?):String {
        if (jsonArray.size() > 0) {
            val jsonItemValue = jsonArray[0]
            if (jsonItemValue.isJsonObject && !jsonItemValue.isJsonArray) {
               val objectType = travelSingleObject(jsonItemValue.asJsonObject, null, key)
               return "$objectType[]"
            } else if (jsonItemValue.isJsonPrimitive) {
                val type = travelPrimitive(jsonItemValue.asJsonPrimitive, null, null)
                return "$type[]"
            }
        }
        return "any[]"
    }

    override fun travelSingleObject(jsonObject: JsonObject, parentJsonElement: JsonElement?, key: String?):String {
        var doc = "/**\n"
        val typeName = toCamelcase(key ?: rootName)
        doc += "*@typedef {$typeName}\n"
        val set = jsonObject.entrySet()
        for (entry in set) {
            val value = entry.value
            val entryKey = entry.key
            val camelCaseKey = toCamelcase(key?:"", entryKey)
            // default type is any
            var type = "any"
            when {
                value.isJsonPrimitive -> {
                    type = travelPrimitive(value.asJsonPrimitive, jsonObject, camelCaseKey)
                }
                value.isJsonObject -> {
                    type = travelSingleObject(value.asJsonObject, jsonObject, camelCaseKey)
                }
                value.isJsonArray -> {
                    type = travelArray(value.asJsonArray, jsonObject, camelCaseKey)
                }
                value.isJsonNull -> {
                    type = travelNull(value.asJsonNull, jsonObject, camelCaseKey)
                }
            }
            doc += if (value.isJsonNull) {
                "*@property {$type} [$entryKey]\n"
            } else {
                "*@property {$type} $entryKey\n"
            }


        }
        doc += "*/"
        docsMap[typeName] = doc
        return typeName
    }

    private fun toCamelcase (vararg names: String):String {
       return names.reduce{
            acc, string -> uppercaseFirstChar(acc)+uppercaseFirstChar(string)
        }
    }

    private fun uppercaseFirstChar(key: String): String = key[0].toUpperCase() + key.substring(1, key.length)


}