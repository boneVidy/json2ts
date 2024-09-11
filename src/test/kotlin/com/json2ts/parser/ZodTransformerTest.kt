package com.json2ts.parser

import com.google.gson.JsonSyntaxException
import com.json2ts.parser.typescript.ZodTransformer
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("TooManyFunctions", "MaxLineLength", "SwallowedException")
internal class ZodTransformerTest {
    @Test
    fun `if it is a string value` () {
        val tsParser = ZodTransformer("\"halo\"", "Root")
        assertEquals("if it is a string value", """const Root = z.string();""".trimIndent(), tsParser.toCode())
    }

    @Test
    fun `if it is a number value` () {
        val tsParser = ZodTransformer("123456", "Root")
        assertEquals("if it is a number value", """const Root = z.number();""".trimIndent(), tsParser.toCode())
    }

    @Test
    fun `if set a customer name`() {
        val tsParser = ZodTransformer("123456", "Response")
        val ret = tsParser.toCode()
        assertEquals("if set a customer name", """const Response = z.number();""".trimIndent(), ret)
    }
//
    @Test
    fun `if it is a single object`() {
        val tsParser =
            ZodTransformer("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Root")
        val ret = tsParser.toCode()
    val expected = "const Root = z.object({\n"+
            "\tname: z.string(),\n"+
            "\tchild: z.array(z.number()),\n"+
            "\tage: z.number()\n"+
            "});"
        assertEquals(
            "if it is a single object",
            expected
            , ret
        )

    }


    @Test
    fun `if it is a single object and has a property is a array`() {
        val tsParser =
            ZodTransformer("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Root")
        val ret = tsParser.toCode()
        val expected = "const Root = z.object({\n"+
                "\tname: z.string(),\n"+
                "\tchild: z.array(z.number()),\n"+
                "\tage: z.number()\n"+
                "});"
        assertEquals(
            "if it is a single object and has a property is a array",
            expected
            , ret
        )

    }

    @Test
    fun `if it is a nested object include array, and the array include object`() {
        val json = """{"name":"vidy","age":33,"child":[{"name":"susy","age":3}]}"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        // expected style with \t and \n
        val expected = "const Root = z.object({\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number(),\n"+
                "\tchild: z.array(z.object({\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number()\n"+
                "}))\n"+
                "});"
        assertEquals(
            "if it is a nested object include array, and the array include object",
            expected
            , ret
        )
    }

    @Test
    fun `if json is a object include object`() {
        val json = """{"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3}}"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = "const Root = z.object({\n"+
                "\tmale: z.boolean(),\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number(),\n"+
                "\tchild: z.object({\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number()\n"+
                "})\n"+
                "});"
        assertEquals(
            "if json is a object include object",
            expected
            , ret
        )
    }

    @Test
    fun `if json is a object include object has nested object`() {
        val json =
            """
                {"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3,"Github":{"url":"https://github.com"}}}
            """.trimIndent()
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = "const Root = z.object({\n"+
                "\tmale: z.boolean(),\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number(),\n"+
                "\tchild: z.object({\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number(),\n"+
                "\tGithub: z.object({\n"+
                "\turl: z.string()\n"+
                "})\n"+
                "})\n"+
                "});"
        assertEquals(
            "if json is a object include object has nested object",
            expected
            , ret
        )
    }


    @Test
    fun `if json is a object include null property`() {
        val json = """{"name":"aaa","age":60,"child":null}"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = "const Root = z.object({\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number(),\n"+
                "\tchild: z.any()\n"+
                "});"
        assertEquals(
            "if json is a object include null property",
            expected
            , ret
        )
    }



    @Test
    fun `if json is a array`() {
        val json = """[1,2,3]"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = """const Root = z.array(z.number());"""
        assertEquals(
            "if json is a array",
            expected
            , ret
        )
    }

    @Test
    fun `if json is a empty array`() {
        val json = """[]"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = """const Root = z.array(z.any());"""
        assertEquals(
            "if json is a empty array",
            expected
            , ret
        )
    }

    @Test
    fun `if json is a array of object`() {
        val json = """[{"name":"vidy","age":30}]"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = "const Root = z.array(z.object({\n"+
                "\tname: z.string(),\n"+
                "\tage: z.number()\n"+
                "}));"
        assertEquals("if json is a array of object", expected, ret)
    }

    @Test
    fun `when parse a valid json, throw a exception`() {
        val json = """{"name":"vidy""""

        try {
            ZodTransformer(json, "Root")
        } catch (e: JsonSyntaxException) {
            assert(true) {
                "when parse a valid json, throw a exception"
            }
        }
    }

    @Test
    fun `object's property name has space`() {
        val json = """{"na me":"vidy","age":30}"""
        val tsParser = ZodTransformer(json, "Root")
        val ret = tsParser.toCode()
        val expected = "const Root = z.object({\n"+
                "\t\"na me\": z.string(),\n"+
                "\tage: z.number()\n"+
                "});"
        assertEquals("if the object's property name has space", expected, ret)
    }


}
