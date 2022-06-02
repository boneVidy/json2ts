package com.json2ts.parser


import com.google.gson.JsonSyntaxException
import icons.com.json2ts.parser.TsConverter
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("TooManyFunctions", "MaxLineLength", "SwallowedException")
internal class TsConverterTest {

    @Test
    fun rootPrimitiveTest() {
        val tsParser = TsConverter("123456", "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals("rootPrimitiveTest", """export type Root = number;""".trimIndent(), ret)
    }

    @Test
    fun customerRootNamePrimitiveTest() {
        val tsParser = TsConverter("123456", "Response", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals("if set a customer name", """export type Response = number;""".trimIndent(), ret)
    }

    @Test
    fun singleObjectTest() {
        val tsParser =
            TsConverter("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "singleObjectTest",
            "export interface Root {\n\tname: string;\n\tchild: number[];\n\tage: number;\n}", ret
        )
    }

    @Test
    fun convertObjectToTsTypeStruct() {
        val tsParser =
            TsConverter("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Root", ParseType.TypeStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "singleObjectTest",
            "export type Root = {\n\tname: string;\n\tchild: number[];\n\tage: number;\n}", ret
        )
    }

    @Test
    fun singleObjectWithCustomerNameTest() {
        val tsParser =
            TsConverter("{\"name\":\"vidy\", \"child\": [1,2],\"age\":33}", "Response", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "if it is a Object(not Array) and has a customer name",
            "export interface Response {\n\tname: string;\n\tchild: number[];\n\tage: number;\n}",
            ret
        )
    }

    @Test
    fun nestedObjectIncludeArrayTest() {
        val json = """{"name":"vidy","age":33,"child":[{"name":"susy","age":3}]}"""
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "nestedObjectIncludeArrayTest",
"""export interface Root {
	name: string;
	age: number;
	child: RootChild[];
}
export interface RootChild {
	name: string;
	age: number;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectWithCustomerNameIncludeArrayTest() {
        val json = """{"name":"vidy","age":33,"child":[{"name":"susy","age":3}]}"""
        val tsParser = TsConverter(json, "Response", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "if is a nested object with customer name and include array",
"""export interface Response {
	name: string;
	age: number;
	child: ResponseChild[];
}
export interface ResponseChild {
	name: string;
	age: number;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectIncludeObjectTest() {
        val json = """{"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3}}"""
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "nestedObjectIncludeObjectTest",
            """export interface Root {
	male: boolean;
	name: string;
	age: number;
	child: RootChild;
}
export interface RootChild {
	name: string;
	age: number;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectIncludeObjectHasNestedObjectTest() {
        val json =
            """
                {"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3,"Github":{"url":"https://github.com"}}}
            """.trimIndent()
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "nestedObjectIncludeObjectTest",
            """export interface Root {
	male: boolean;
	name: string;
	age: number;
	child: RootChild;
}
export interface RootChildGithub {
	url: string;
}
export interface RootChild {
	name: string;
	age: number;
	Github: RootChildGithub;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectWithCustomerNameIncludeObjectTest() {
        val json = """{"male":true,"name":"vidy","age":33,"child":{"name":"susy","age":3}}"""
        val tsParser = TsConverter(json, "Response", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "if object with customer name include object property",
            """export interface Response {
	male: boolean;
	name: string;
	age: number;
	child: ResponseChild;
}
export interface ResponseChild {
	name: string;
	age: number;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun nestedObjectTestIncludeNullProperty() {
        val json = """{"name":"aaa","age":60,"child":null}"""
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "if nestedObjectTest include a null property",
            """export interface Root {
	name: string;
	age: number;
	child?: any;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun nullTest() {
        val json = """null"""
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals("if json is a null object", """export type Root = any;""".trimIndent(), ret)
    }

//    @Test
//    fun rootArrayTest() {
//        val json = """[1,2,3]"""
//        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
//        val ret = tsParser.toCode()
//        assertEquals("if json is a primitive array", """export type Root = number[];""".trimIndent(), ret)
//    }

    @Test
    fun rootEmptyArrayTest() {
        val json = """[]"""
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals("if json is a empty array", """export type Root = any[];""".trimIndent(), ret)
    }

    @Test
    fun rootArrayObjectTest() {
        val json = """[{"name":"vidy","age":30}]"""
        val tsParser = TsConverter(json, "Root", ParseType.InterfaceStruct)
        val ret = tsParser.toCode()
        assertEquals(
            "if json is a object array",
"""export type Root = RootChild[];
export interface RootChild {
	name: string;
	age: number;
}""".trimIndent(),
            ret
        )
    }

    @Test
    fun testInvalidJsonSting() {
        val json = """{"name":"vidy""""

        try {
            TsConverter(json, "Root", ParseType.InterfaceStruct)
        } catch (e: JsonSyntaxException) {
            assert(true) {
                "when parse a valid json, throw a exception"
            }
            return
        }
    }
}
