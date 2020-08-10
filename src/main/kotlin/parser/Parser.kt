package parser

import com.google.gson.*

fun toTypescript(
    jsonString: String,
    objectName: String = "RootObject",
    parseType: ParseType = ParseType.InterfaceStruct
): String {
    val jsonElement = try {
        JsonParser().parse(jsonString)
    } catch (e: JsonSyntaxException) {
        return e.message ?: "parser error";
    }
    if (jsonElement.isJsonArray) {
        val jsonArray = jsonElement.asJsonArray;
        if (jsonArray.size() == 0) {
            return "export type $objectName = any[];";
        }
        val firstEle = jsonArray[0]
        if (firstEle.isJsonPrimitive) {
            val primitive = firstEle.asJsonPrimitive;
            return toPrimitiveArray(objectName, primitive)
        }
        val typeStr = toTsStruct(jsonElement.asJsonArray[0], parseType, "RootDataItem");
        return """$typeStr; 
export type ${objectName}s = RootDataItem[];
"""
    }
    return toTsStruct(jsonElement, parseType, objectName)
}

private fun toPrimitiveArray(name: String, jsonPrimitive: JsonPrimitive?): String = when (jsonPrimitive) {
    null -> {
        "export type $name = any[];";
    }
    else -> when {
        jsonPrimitive.isBoolean -> {
            "export type $name = boolean[];";
        }
        jsonPrimitive.isString -> {
            "export type $name = string[];";
        }
        jsonPrimitive.isNumber -> {
            "export type $name = number[];";
        }
        else -> "export type $name = any[];";
    }
}

private fun uppercaseFirstChar(key: String): String = key[0].toUpperCase() + key.substring(1, key.length)

private fun lowercaseFirstChar(key: String): String = key[0].toLowerCase() + key.substring(1, key.length)

private fun toTsStruct(
    jsonElement: JsonElement,
    parseType: ParseType,
    objectName: String = "RootObject",
    structResult: MutableList<String> = mutableListOf()
): String {
    val optionalKeys = mutableListOf<String>()
    val objectResult = mutableListOf<String>()
    val json = jsonElement.asJsonObject
    val entrySet = json.entrySet();

    for (o in entrySet) {
        val value = o.value
        val key = o.key
        when {
            value.isJsonObject && !value.isJsonArray -> {
                val childObjectName = uppercaseFirstChar(key)
                val typeString = toTsStruct(
                    jsonElement = value,
                    parseType = parseType,
                    objectName = childObjectName,
                    structResult = structResult
                )
                if (!structResult.contains(typeString)) {
                    objectResult.add(typeString)
                    structResult.add(typeString)
                }
                json.addProperty(key, "$childObjectName;")
            }
            value.isJsonArray -> {
                val valueArr = value.asJsonArray
                val arrayTypes = detectMultiArrayTypes(value)
                if (arrayTypes.size > 1) {
                    val multiArrayBrackets = getMultiArrayBrackets(value.toString())
                    if (isAllEquals(arrayTypes)) {
                        json.addProperty(key, arrayTypes[0].replace("[]", multiArrayBrackets))

                    } else {
                        json.addProperty(key, "any$multiArrayBrackets;")
                    }

                } else if (valueArr.size() > 0 && valueArr[0].isJsonObject) {
                    val childObjectName = uppercaseFirstChar(key)
                    objectResult.add(toTsStruct(valueArr[0], parseType, childObjectName, structResult))
                    json.addProperty(key, removeMajority(childObjectName) + "[];")
                } else {
                    json.addProperty(key, arrayTypes[0])
                }
            }
            value.isJsonPrimitive -> {
                val jsonPrimitive = value.asJsonPrimitive;
                when {
                    jsonPrimitive.isBoolean -> {
                        json.addProperty(key, "boolean;");
                    }
                    jsonPrimitive.isString -> {
                        json.addProperty(key, "string;");
                    }
                    jsonPrimitive.isNumber -> {
                        json.addProperty(key, "number;");
                    }
                }
            }
            else -> {
                json.addProperty(key, "any;")
                optionalKeys.add(key)
            }
        }
    }
    val result = formatCharsToTypeScript(json, objectName, optionalKeys, parseType)
    objectResult.add(result)
    return objectResult.joinToString("\n\n")
}

private fun isAllEquals(array: List<String>): Boolean {
    val fist = array[0];
    val list = array.slice(1..array.size)
    for (item in list) {
        if (item != fist) {
            return false
        }
    }
    return true
}

private fun getMultiArrayBrackets(content: String): String {
    var brackets = "";
    content.forEach { char ->
        run {
            if (char == '[') {
                brackets = "$brackets[]"
            }
        }
    }
    return brackets
}

private fun checkIsString(item: JsonElement): Boolean = item.isJsonPrimitive && item.asJsonPrimitive.isString

private fun checkIsNumber(item: JsonElement): Boolean = item.isJsonPrimitive && item.asJsonPrimitive.isNumber

private fun checkIsBoolean(item: JsonElement): Boolean = item.isJsonPrimitive && item.asJsonPrimitive.isBoolean

private fun checkJsonArrayType(jsonArray: JsonArray, checkFn: (item: JsonElement) -> Boolean): Boolean {
    var ret = true
    jsonArray.forEach { item -> ret = checkFn(item) }
    return ret
}

private fun detectMultiArrayTypes(
    value: JsonElement,
    valueType: MutableList<String> = mutableListOf<String>()
): MutableList<String> {
    val list = mutableListOf<String>()
    list.addAll(valueType)
    if (value.isJsonArray) {
        val arr = value.asJsonArray;
        when {
            arr.size() == 0 -> {
                list.add("any[];")
            }
            arr[0].isJsonArray -> {
                for (item in arr) {
                    val valueTypeResult = detectMultiArrayTypes(item, valueType)
                    list.addAll(valueTypeResult)
                }
            }
            checkJsonArrayType(arr, ::checkIsString) -> {
                list.add("string[];")
            }
            checkJsonArrayType(arr, ::checkIsNumber) -> {
                list.add("number[];")
            }
            checkJsonArrayType(arr, ::checkIsBoolean) -> {
                list.add("boolean[]")
            }
            else -> {
                list.add("any[]")
            }
        }

    }
    return list;
}


private fun removeMajority(objectName: String): String {
    val set = setOf<RegexOption>(RegexOption.IGNORE_CASE);
    val reg = Regex("IES$", set)
    val len = objectName.length
    if (reg.matches(objectName)) {
        return objectName.substring(0, len - 3) + "y"
    } else if (objectName.contains("S")) {
        return objectName.substring(0, len - 1)
    }
    return objectName
}

private fun formatCharsToTypeScript(
    jsonContent: JsonObject,
    objectName: String,
    optionalKeys: List<String>,
    parseType: ParseType
): String {
    val reg = Regex("\"")
    var result = Gson()
        .newBuilder()
        .setPrettyPrinting().create()
        .toJson(jsonContent)
        .replace(reg, "")
        .replace(Regex(","), "")
    val entrySets = jsonContent.entrySet()
    for (item in entrySets) {
        val key = item.key
        result = if (optionalKeys.contains(key)) {
            result.replace(Regex("$key:"), lowercaseFirstChar("$key?:"))
        } else {
            result.replace(Regex("$key:"), lowercaseFirstChar("$key:"))
        }
    }
    val newObjectName = removeMajority(objectName)
    if (parseType == ParseType.TypeStruct) {
        return "export type $newObjectName = $result"
    }
    return "export interface $newObjectName $result"

}
