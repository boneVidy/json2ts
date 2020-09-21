package parser
import com.google.gson.*


abstract class JsonTravel(val jsonString:String) {


    abstract fun travelPrimitive(asJsonPrimitive: JsonPrimitive, parentJsonElement: JsonElement?, key: String?):String

    abstract fun travelNull(jsonNull: JsonNull,  parentJsonElement: JsonElement?, key: String?):String

    abstract fun travelArray(jsonArray: JsonArray,  parentJsonElement: JsonElement?, key: String?):String

    abstract fun travelSingleObject (jsonObject: JsonObject,  parentJsonElement: JsonElement?, key: String?):String

}