package base

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.fail
import parser.ArrayTokensSource
import parser.ITokenSource
import parser.RecursiveParserTest
import tokenizer.Token
import tokenizer.TokenType

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

    fun createTokenSource(vararg tokens: TokenType): ITokenSource {
        return ArrayTokensSource(tokens.mapIndexed { index: Int, tokenType: TokenType -> Token(tokenType, 0, 0, index.toString()) }.toList())
    }

    companion object {
        inline fun <reified T> assertIs(actual: Any) {
            assert(actual is T) { "Expected ${T::class.java.name} but actual is ${actual.javaClass.name}" }
        }

        fun assertFail(string: String): Nothing {
            fail(string)
        }
    }
}