package com.json2ts.parser

import com.google.gson.*

open class TsPrimitiveConverter(): JsonTraverser() {
    override fun traversePrimitive(asJsonPrimitive: JsonPrimitive, parentJsonElement: JsonElement?, key: String?):String {
        return when {
            asJsonPrimitive.isString -> {
                "string"
            }
            asJsonPrimitive.isNumber -> {
                "number"
            }
            asJsonPrimitive.isBoolean -> {
                "boolean"
            }
            else -> "any"
        }
    }

    override fun traverseNull(jsonNull: JsonNull, parentJsonElement: JsonElement?, key: String?):String {
        return "any"
    }

    override fun traverseArray(jsonArray: JsonArray, parentJsonElement: JsonElement?, key: String?):String {
        if (jsonArray.size() > 0) {
            val jsonItemValue = jsonArray[0]
            if (jsonItemValue.isJsonObject && !jsonItemValue.isJsonArray) {
                val objectType = traverseSingleObject(jsonItemValue.asJsonObject, null, key)
                return "$objectType[]"
            } else if (jsonItemValue.isJsonPrimitive) {
                val type = traversePrimitive(jsonItemValue.asJsonPrimitive, null, null)
                return "$type[]"
            }
        }
        return "any[]"
    }

    override fun traverseSingleObject(jsonObject: JsonObject, parentJsonElement: JsonElement?, key: String?): String {
        return "object"
    }

    protected fun toCamelcase(vararg names: String): String {
        return names.reduce { acc, string ->
            uppercaseFirstChar(acc) + uppercaseFirstChar(string)
        }
    }

    protected  fun uppercaseFirstChar(key: String): String = key[0].toUpperCase() + key.substring(1, key.length)
}