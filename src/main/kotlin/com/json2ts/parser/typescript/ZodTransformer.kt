package com.json2ts.parser.typescript

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonNull
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class ZodTransformer(private val jsonString: String, private val rootName: String) : JsonTraver, ToCode {
    private lateinit var rootJsonElement: JsonElement
    private lateinit var zodSchema: String
    init {
        create()
    }

    private fun create() {
        rootJsonElement = JsonParser.parseString(jsonString)
        traverseRoot(rootJsonElement, rootName)
    }

    private fun traverseRoot(jsonElement: JsonElement, key: String?) {
        val schema = traverseElement(jsonElement, key ?: rootName)
        zodSchema = "const $rootName = $schema;"
    }

    private fun traverseElement(jsonElement: JsonElement, key: String): String {
        return when {
            jsonElement.isJsonObject -> traverseSingleObject(jsonElement.asJsonObject, key)
            jsonElement.isJsonArray -> traverseArray(jsonElement.asJsonArray, key)
            jsonElement.isJsonNull -> traverseNull(jsonElement.asJsonNull, key)
            jsonElement.isJsonPrimitive -> traversePrimitive(jsonElement.asJsonPrimitive, key)
            else -> "z.any()"
        }
    }

    override fun traversePrimitive(asJsonPrimitive: JsonPrimitive, key: String?): String {
        return when {
            asJsonPrimitive.isBoolean -> "z.boolean()"
            asJsonPrimitive.isNumber -> "z.number()"
            asJsonPrimitive.isString -> "z.string()"
            else -> "z.any()"
        }
    }

    override fun traverseNull(jsonNull: JsonNull, key: String?): String {
        return "z.any()"
    }

    override fun traverseArray(jsonArray: JsonArray, key: String?): String {
        if (jsonArray.size() == 0) {
            return "z.array(z.any())"
        }
        val firstElement = jsonArray[0]
        val elementType = traverseElement(firstElement, key ?: "")
        return "z.array($elementType)"
    }

    override fun traverseSingleObject(jsonObject: JsonObject, key: String?): String {
        val properties = jsonObject.entrySet().joinToString(",\n") { entry ->
            var propertyKey = entry.key
            // if the key contains special characters or the key is number, we need to wrap it in quotes
            if (!propertyKey.matches(Regex("^[a-zA-Z_$][a-zA-Z\\d_$]*$"))) {
                propertyKey = "\"$propertyKey\""
            }
            val propertyType = traverseElement(entry.value, propertyKey)
            "\t$propertyKey: $propertyType"
        }
        return "z.object({\n$properties\n})"
    }

    override fun toCode(): String {
        return zodSchema
    }
}
