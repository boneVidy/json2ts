package parser

import com.google.gson.*
import exceptions.SyntaxException

fun parserFromJsonString(src: String): JsonElement? {
    return JsonParser().parse(src).asJsonObject
}

fun toTypescript(jsonString: String): String {
    val jsonContent = try {
        parserFromJsonString(jsonString)
    } catch (e: Exception) {
        throw SyntaxException()
    }
    if (jsonContent == null) {
        return "null"
    }
    if (jsonContent.isJsonArray) {
        return toTsStruct(jsonContent.asJsonArray[0])
    }
    return toTsStruct(jsonContent)
}

fun upcateFirstChar(key: String): String {
    return key[0].toUpperCase() + key.substring(1, key.length)
}
fun lowcaseFirstChar(key: String): String {
    return key[0].toLowerCase() + key.substring(1, key.length)
}
fun toTsStruct(jsonElement: JsonElement, objectName: String = "RootObject"):String {
    val optionalKeys = mutableListOf<String>()
    val objectResult = mutableListOf<String>()
    val json = jsonElement.asJsonObject
    val entrySet = json.entrySet();

    for (o in entrySet) {
        val value = o.value
        val key = o.key
        if (value.isJsonObject && !value.isJsonArray) {

            val childObjectName = upcateFirstChar(key)
            objectResult.add(toTsStruct(value, childObjectName))
            json.addProperty(key, "$childObjectName;")
//            json[o.key] = jsonElement
        } else if (value.isJsonArray) {
            val valueArr = value.asJsonArray
            val arrayTypes = detectMultiArrayTypes(value)
            if (arrayTypes.size > 1) {
                val multiArrayBrackets= getMultiArrayBrackets(value.toString())
                if (isAllEquals(arrayTypes)) {
                    json.addProperty(key, arrayTypes[0].replace("[]",multiArrayBrackets))

                } else {
                    json.addProperty(key, "any"+multiArrayBrackets+";")
                }

            } else if (valueArr.size() > 0 && valueArr[0].isJsonObject) {
                val childObjectName = upcateFirstChar(key)
                objectResult.add(toTsStruct(valueArr[0],childObjectName))
                json.addProperty(key,removeMajority(childObjectName)+"[];")
            } else {
                json.addProperty(key, arrayTypes[0])
            }
        } else if (value.isJsonPrimitive) {
            val jsonPrimitive = value.asJsonPrimitive;
            if (jsonPrimitive.isBoolean) {
                json.addProperty(key, "boolean;");
            } else if (jsonPrimitive.isString) {
                json.addProperty(key, "string;");
            } else if (jsonPrimitive.isNumber) {
                json.addProperty(key, "number;");
            }
        } else {
            json.addProperty(key, "any;")
            optionalKeys.add(key)
        }
    }
    val result = formatCharsToTypeScript(json, objectName, optionalKeys)
    objectResult.add(result)
    return objectResult.joinToString("\n\n")
}
fun toJsonEle (src: String):JsonElement {
    return JsonParser().parse(src)
}
fun isAllEquals (array: List<String>):Boolean {
    val fist = array[0];
    val list = array.slice(1..array.size)
    for (item in list) {
        if (item != fist) {
            return false
        }
    }
    return true
}
fun getMultiArrayBrackets(content: String): String {
    var brackets = "";
    content.forEach { char ->
        run {
            if (char == '[') {
                brackets = brackets + "[]"
            }
        }
    }
    return brackets
}
fun checkIsString (item: JsonElement):Boolean {
    return item.isJsonPrimitive && item.asJsonPrimitive.isString
}
fun checkIsNumber (item: JsonElement):Boolean {
    return item.isJsonPrimitive && item.asJsonPrimitive.isNumber
}
fun checkIsBoolean (item: JsonElement):Boolean {
    return item.isJsonPrimitive && item.asJsonPrimitive.isBoolean
}
fun checkJsonArrayType (jsonArray: JsonArray, checkFn: (item:JsonElement) -> Boolean):Boolean {
    var ret = true
    jsonArray.forEach { item -> ret = checkFn(item) }
    return ret
}
fun detectMultiArrayTypes(value: JsonElement, valueType: MutableList<String> = mutableListOf<String>()): MutableList<String> {
    val list = mutableListOf<String>()
    list.addAll( valueType)
    if (value.isJsonArray) {
        val arr = value.asJsonArray;
        if (arr.size() == 0) {
            list.add("any[];")
        } else if(arr[0].isJsonArray) {
            for (item in arr) {
               val  valueTypeResult = detectMultiArrayTypes(item, valueType)
                list.addAll(valueTypeResult)
            }
        } else if (checkJsonArrayType(arr, ::checkIsString)) {
            list.add("string[];")
        } else if (checkJsonArrayType(arr, ::checkIsNumber)) {
            list.add("number[];")
        } else if (checkJsonArrayType(arr,::checkIsBoolean)) {
            list.add("boolean[]")
        } else{
            list.add("any[]")
        }

    }
    return list;
}


fun removeMajority(objectName: String): String{
    val set = setOf<RegexOption>(RegexOption.IGNORE_CASE);
    val reg = Regex("IES$", set)
    val len = objectName.length
    if (reg.matches(objectName)) {
        return objectName.substring(0, len - 3) + "y"
    } else if (objectName.contains("S")) {
        return objectName.substring(0, len -1)
    }
    return objectName
}
fun formatCharsToTypeScript(jsonContent: JsonObject, objectName: String, optionalKeys: List<String>): String {
    val reg = Regex("\"")
    var result = Gson()
        .newBuilder()
        .setPrettyPrinting().create()
        .toJson(jsonContent)
        .replace(reg, "")
        .replace(Regex(","),"")

    val entrySets = jsonContent.entrySet()

    for (item in entrySets) {
        val key = item.key
        if (optionalKeys.contains(key)) {
            result = result.replace(Regex("$key:"), lowcaseFirstChar("$key:?"))
        } else {
            result = result.replace(Regex("$key:"), lowcaseFirstChar("$key:"))
        }
    }
    val newObjectName = removeMajority(objectName)
    return "export interface $newObjectName $result"
}


fun main () {
    val ret = toTypescript("{\n" +
            "    \"success\": true,\n" +
            "    \"data\": {\n" +
            "        \"loginname\": \"alsotang\",\n" +
            "        \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\",\n" +
            "        \"githubUsername\": \"alsotang\",\n" +
            "        \"create_at\": \"2012-09-09T05:26:58.319Z\",\n" +
            "        \"score\": 15700,\n" +
            "        \"recent_topics\": [\n" +
            "            {\n" +
            "                \"id\": \"5c6d11d033b0b629ac8434ef\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"【深圳】腾讯云加速产品中心--前端工程师\",\n" +
            "                \"last_reply_at\": \"2019-05-11T04:22:18.616Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5bd4772a14e994202cd5bdb7\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"服务器迁移至 aws 日本机房\",\n" +
            "                \"last_reply_at\": \"2019-09-09T07:21:41.870Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5b9b5d2ba5ed9d2159fa312e\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"cnode社区静态资源域名改造\",\n" +
            "                \"last_reply_at\": \"2018-10-27T14:04:54.155Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5b629556b71aedfe4c12667c\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"开发了一个腾讯云 Node.js SDK\",\n" +
            "                \"last_reply_at\": \"2018-08-02T05:31:06.244Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5843092c3ebad99b336b1d48\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"使用 generator 按行读取文件的库，co-readline\",\n" +
            "                \"last_reply_at\": \"2016-12-04T13:57:30.730Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"58351689bde2b59e06141e9f\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"【腾讯】各种岗位均可内推，前后端均可\",\n" +
            "                \"last_reply_at\": \"2019-08-19T12:26:37.201Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"580ddc2eeae2a24f34e67e69\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"这，就是技术人的江湖\",\n" +
            "                \"last_reply_at\": \"2018-10-17T08:43:54.082Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"580460a5fdf3bd3d651186d1\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"推荐你心中的CNode「极客代言人」，打造《中国技术社群英雄谱》\",\n" +
            "                \"last_reply_at\": \"2016-10-24T04:09:13.002Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"57ee19c93670ca3f44c5bfde\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"从url中解析出域名、子域名和有效顶级域名\",\n" +
            "                \"last_reply_at\": \"2017-04-11T01:47:09.793Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"57e917e2bb55ef3e1a17fcbd\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"https 免费证书获取指引\",\n" +
            "                \"last_reply_at\": \"2017-06-22T01:14:19.918Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"57e2520a7e77820e3acfe0ed\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"【深圳】腾讯云 CDN 前端团队诚招高级工程师\",\n" +
            "                \"last_reply_at\": \"2016-11-17T13:21:42.774Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"57c6a1d492fad7e46b4169b5\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"一个模块：forceinterval，可无缝替换许多 setInterval 的场景\",\n" +
            "                \"last_reply_at\": \"2016-08-31T10:17:33.671Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5759bef0e5fa62531af6e151\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"async/await 比 yield 好在哪里？\",\n" +
            "                \"last_reply_at\": \"2016-06-22T11:31:49.232Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"572afb6b15c24e592c16e1e6\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"新的社区推荐客户端：Noder\",\n" +
            "                \"last_reply_at\": \"2016-07-07T13:24:42.321Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"570924d294b38dcb3c09a7a0\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"alsotang\",\n" +
            "                    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/1147375?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"timer 的 unref 函数\",\n" +
            "                \"last_reply_at\": \"2017-10-11T10:55:47.441Z\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"recent_replies\": [\n" +
            "            {\n" +
            "                \"id\": \"5dfc8d180696c446bf64f5d5\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"zhangcheng-RunRun\",\n" +
            "                    \"avatar_url\": \"https://avatars3.githubusercontent.com/u/59083067?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"【1封新邀请】5大主题干货满满 ECUG For Future技术盛宴等你参加！\",\n" +
            "                \"last_reply_at\": \"2019-12-25T14:24:01.372Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5df243e2df1b9a40d14c6504\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"kenghuang\",\n" +
            "                    \"avatar_url\": \"https://avatars3.githubusercontent.com/u/24793336?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"社区推荐的安卓版客户端为啥不能回复\",\n" +
            "                \"last_reply_at\": \"2019-12-15T03:38:59.776Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5de668e639af564604bc084f\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"cheunghy\",\n" +
            "                    \"avatar_url\": \"https://avatars0.githubusercontent.com/u/3055936?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"业内知名技术大神创业财务吃紧，地价接外包，可开发 web 前后端，安卓， iOS，公众号等\",\n" +
            "                \"last_reply_at\": \"2019-12-12T09:36:39.462Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5de5cc0d6043397a546db184\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"chenkai0520\",\n" +
            "                    \"avatar_url\": \"https://avatars2.githubusercontent.com/u/30174970?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"一个javascript日期的坑\",\n" +
            "                \"last_reply_at\": \"2019-12-06T13:36:35.126Z\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"5dbffffdece3813ad9ba63cf\",\n" +
            "                \"author\": {\n" +
            "                    \"loginname\": \"biggerV\",\n" +
            "                    \"avatar_url\": \"https://avatars3.githubusercontent.com/u/21165821?v=4&s=120\"\n" +
            "                },\n" +
            "                \"title\": \"使用Vue开发了一个WebOS\",\n" +
            "                \"last_reply_at\": \"2019-11-20T03:56:42.637Z\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}");
    println(ret)
}