package com.json2ts.parser.typescript

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

open class TsPrimitiveConverter : JsonTraver {
    override fun traversePrimitive(asJsonPrimitive: JsonPrimitive, key: String?): String {
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

    override fun traverseNull(jsonNull: JsonNull, key: String?): String {
        return "any"
    }

    override fun traverseArray(jsonArray: JsonArray, key: String?): String {
        var ret = ""
        if (jsonArray.size() > 0) {
            val jsonItemValue = jsonArray[0]
            if (jsonItemValue.isJsonObject && !jsonItemValue.isJsonArray) {
                val objectType = traverseSingleObject(jsonItemValue.asJsonObject, key)
                ret = "$objectType[]"
            } else if (jsonItemValue.isJsonPrimitive) {
                val type = traversePrimitive(jsonItemValue.asJsonPrimitive, null)
                ret = "$type[]"
            }
            return ret
        }
        return "any[]"
    }

    override fun traverseSingleObject(jsonObject: JsonObject, key: String?): String {
        return "object"
    }

    protected fun toCamelcase(vararg names: String): String {
        return names.reduce { acc, string ->
            (uppercaseFirstChar(acc) + uppercaseFirstChar(string)).replace(" ", "")
        }
    }

    private fun uppercaseFirstChar(key: String): String = key[0].uppercaseChar() + key.substring(1, key.length)
}
