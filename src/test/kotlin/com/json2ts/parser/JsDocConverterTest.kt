package com.json2ts.parser



import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("TooManyFunctions", "MaxLineLength", "SwallowedException")
internal class JsDocConverterTest {

    @Test
    fun rootPrimitiveTest() {
        val jsonDocParser = JsDocConverter("123456", "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "rootPrimitiveTest",
            """
            /**
            *@typedef Root
            *@type {number}
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun customerRootNamePrimitiveTest() {
        val jsonDocParser = JsDocConverter("123456", "Response")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if set a customer name",
            """
            /**
            *@typedef Response
            *@type {number}
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun singleObjectTest() {
        val jsonDocParser = JsDocConverter("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "singleObjectTest",
            """
            /**
            *@typedef Root
            *@property {string} name
            *@property {number[]} child
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun singleObjectWithCustomerNameTest() {
        val jsonDocParser = JsDocConverter("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Response")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if it is a Object(not Array) and has a customer name",
            """
            /**
            *@typedef Response
            *@property {string} name
            *@property {number[]} child
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectIncludeArrayTest() {
        val json = """{"name":"vidy","age":33,"child":[{"name":"susy","age":3}]}"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "nestedObjectIncludeArrayTest",
            """
            /**
            *@typedef Root
            *@property {string} name
            *@property {number} age
            *@property {RootChild[]} child
            */
            /**
            *@typedef RootChild
            *@property {string} name
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectWithCustomerNameIncludeArrayTest() {
        val json = """{"name":"vidy","age":33,"child":[{"name":"susy","age":3}]}"""
        val jsonDocParser = JsDocConverter(json, "Response")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if is a nested object with customer name and include array",
            """
            /**
            *@typedef Response
            *@property {string} name
            *@property {number} age
            *@property {ResponseChild[]} child
            */
            /**
            *@typedef ResponseChild
            *@property {string} name
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectIncludeObjectTest() {
        val json = """{"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3}}"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "nestedObjectIncludeObjectTest",
            """
            /**
            *@typedef Root
            *@property {boolean} male
            *@property {string} name
            *@property {number} age
            *@property {RootChild} child
            */
            /**
            *@typedef RootChild
            *@property {string} name
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectWithCustomerNameIncludeObjectTest() {
        val json = """{"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3}}"""
        val jsonDocParser = JsDocConverter(json, "Response")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if object with customer name include object property",
            """
            /**
            *@typedef Response
            *@property {boolean} male
            *@property {string} name
            *@property {number} age
            *@property {ResponseChild} child
            */
            /**
            *@typedef ResponseChild
            *@property {string} name
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectTestIncludeNullProperty() {
        val json = """{"name":"aaa","age":60,"child":null}"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if nestedObjectTest include a null property",
            """
            /**
            *@typedef Root
            *@property {string} name
            *@property {number} age
            *@property {any} [child]
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun nullTest() {
        val json = """null"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if json is a null object",
            """
                    /**
                    *@typedef Root
                    *@type {any}
                    */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun rootArrayTest() {
        val json = """[1,2,3]"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if json is a primitive array",
            """
            /**
            *@typedef Root
            *@type {number[]}
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun rootEmptyArrayTest() {
        val json = """[]"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if json is a empty array",
            """
            /**
            *@typedef Root
            *@type {any[]}
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun rootArrayObjectTest() {
        val json = """[{"name":"vidy","age":30}]"""
        val jsonDocParser = JsDocConverter(json, "Root")
        val ret = jsonDocParser.toCode()
        assertEquals(
            "if json is a object array",
            """
            /**
            *@typedef Root
            *@type {RootChild[]}
            */
            /**
            *@typedef RootChild
            *@property {string} name
            *@property {number} age
            */
            """.trimIndent(),
            ret
        )
    }

    @Test
    fun testInvalidJsonSting() {
        val json = """{"name":"vidy""""

        try {
            JsDocConverter(json, "Root")
        } catch (e: JsonSyntaxException) {
            assert(true) {
                "when parse a valid json, throw a exception"
            }
            return
        }

    }
}
