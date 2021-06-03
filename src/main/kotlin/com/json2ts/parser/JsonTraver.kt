package com.json2ts.parser

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

interface JsonTraver {

    fun traversePrimitive(asJsonPrimitive: JsonPrimitive, key: String?): String

    fun traverseNull(jsonNull: JsonNull, key: String?): String

    fun traverseArray(jsonArray: JsonArray, key: String?): String

    fun traverseSingleObject(jsonObject: JsonObject, key: String?): String
}
