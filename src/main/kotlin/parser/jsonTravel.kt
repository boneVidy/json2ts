package parser
import com.google.gson.*


abstract class JsonTraverser(val jsonString:String) {


    abstract fun traversePrimitive(asJsonPrimitive: JsonPrimitive, parentJsonElement: JsonElement?, key: String?):String

    abstract fun traverseNull(jsonNull: JsonNull, parentJsonElement: JsonElement?, key: String?):String

    abstract fun traverseArray(jsonArray: JsonArray, parentJsonElement: JsonElement?, key: String?):String

    abstract fun traverseSingleObject (jsonObject: JsonObject, parentJsonElement: JsonElement?, key: String?):String

}