package base

import org.junit.jupiter.api.Assertions
import parser.RecursiveParserTest
import tokenizer.Token

open class BaseTest {
    protected fun getFileContent(name: String): String {
        return RecursiveParserTest::class.java.classLoader.getResource(name)!!.readText()
    }

    fun <T> assertCollectionEquals(expected: Collection<T>, actual: Collection<T>) {
        Assertions.assertEquals(expected.size, actual.size)
        for (i in expected.indices) {
            Assertions.assertEquals(expected.elementAt(i), actual.elementAt(i))
        }
    }

    fun assertEqualsToken(expected: Token, actual: Token) {
        val equals = expected.type == actual.type &&
                expected.startIndex == actual.startIndex &&
                expected.endIndex == actual.endIndex &&
                expected.value == actual.value
        assert(equals) { "expected:${expected}|actual:${actual}" }
    }

    inline fun <reified T> assertIs(actual: Any) {
        assert(actual is T) { "Expected ${T::class.java.name} but actual is ${actual.javaClass.name}" }
    }
}