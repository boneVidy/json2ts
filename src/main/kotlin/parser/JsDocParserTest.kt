package parser

import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFails

internal class JsDocParserTest {

    @Test
    fun rootPrimitiveTest() {
        val jsonDocParser = JsDocParser("123456", "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("rootPrimitiveTest", """
            /**
            *@typedef {Root}
            *@type {number}
            */
        """.trimIndent(), ret)
    }

    @Test
    fun singleObjectTest() {
        val jsonDocParser = JsDocParser("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("singleObjectTest", """
            /**
            *@typedef {Root}
            *@property {string} name
            *@property {number[]} child
            *@property {number} age
            */
        """.trimIndent(), ret)
    }

    @Test
    fun nestedObjectIncludeArrayTest() {
        val json = """{"name":"vidy","age":33,"child":[{"name":"susy","age":3}]}"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("nestedObjectIncludeArrayTest", """
            /**
            *@typedef {Root}
            *@property {string} name
            *@property {number} age
            *@property {RootChild[]} child
            */
            /**
            *@typedef {RootChild}
            *@property {string} name
            *@property {number} age
            */
        """.trimIndent(), ret)
    }
    @Test
    fun nestedObjectIncludeObjectTest() {
        val json = """{"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3}}"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("nestedObjectIncludeObjectTest", """
            /**
            *@typedef {Root}
            *@property {boolean} male
            *@property {string} name
            *@property {number} age
            *@property {RootChild} child
            */
            /**
            *@typedef {RootChild}
            *@property {string} name
            *@property {number} age
            */
        """.trimIndent(), ret)
    }
    @Test
    fun nestedObjectTestIncludeNullProperty() {
        val json = """{"name":"aaa","age":60,"child":null}"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("if nestedObjectTest include a null property", """
            /**
            *@typedef {Root}
            *@property {string} name
            *@property {number} age
            *@property {any} [child]
            */
        """.trimIndent(), ret)
    }

    @Test
    fun nullTest() {
        val json = """null"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("if json is a null object", """
                    /**
                    *@typedef {Root}
                    *@type {any}
                    */
                """.trimIndent(), ret)
    }

    @Test
    fun rootArrayTest() {
        val json = """[1,2,3]"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("if json is a primitive array", """
            /**
            *@typedef {Root}
            *@type {number[]}
            */
        """.trimIndent(), ret)
    }

    @Test
    fun rootEmptyArrayTest() {
        val json = """[]"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("if json is a empty array", """
            /**
            *@typedef {Root}
            *@type {any[]}
            */
        """.trimIndent(), ret)
    }
    @Test
    fun rootArrayObjectTest() {
        val json = """[{"name":"vidy","age":30}]"""
        val jsonDocParser = JsDocParser(json, "Root")
        val ret = jsonDocParser.toRawStringDoc()
        assertEquals("if json is a object array", """
            /**
            *@typedef {Root}
            *@type {RootChild[]}
            */
            /**
            *@typedef {RootChild}
            *@property {string} name
            *@property {number} age
            */
        """.trimIndent(), ret)
    }

    @Test
    fun testInvalidJsonSting() {
        val json = """{"name":"vidy""""

        try {
            JsDocParser(json, "Root")
        } catch (e:JsonSyntaxException) {
            assert(true) {
                "when parse a valid json, throw a exception"
            }
            return
        }
            assertFails("when parse a valid json, didn't throw a exception") {

        }
    }
}