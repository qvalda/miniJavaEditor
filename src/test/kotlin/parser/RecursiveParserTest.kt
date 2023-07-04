package parser

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tokenizer.ArrayTokensSource
import tokenizer.TokenType
import tokenizer.Tokenizer

open class CodeTestBase {
    protected fun getFileContent(name : String): String {
        return RecursiveParserTest::class.java.classLoader.getResource(name)!!.readText()
    }

    fun <T> assertCollectionEquals(expected: Collection<T>, actual: Collection<T>) {
        Assertions.assertEquals(expected.size, actual.size)
        for (i in expected.indices) {
            Assertions.assertEquals(expected.elementAt(i), actual.elementAt(i))
        }
    }
}

class RecursiveParserTest : CodeTestBase() {

    @ParameterizedTest
    @ValueSource(strings = arrayOf("binarysearch.javam","binarytree.javam","bubblesort.javam","factorial.javam","linearsearch.javam","linkedlist.javam","quicksort.javam","treevisitor.javam"))
    fun parse(name: String) {
        parseFile(name)
    }

    private fun parseFile(name : String) {
        val code = getFileContent(name)
        val t =
            Tokenizer().getTokens(code).filter { t -> t.type != TokenType.Comment }
                .toTypedArray()
        val ts = ArrayTokensSource(t)
        val p = RecursiveParser(ts).parse()
        assert(ts.isEOF())
        //Assertions.assertEquals(TokenType.EOF, ts.currentToken.type)
    }

}