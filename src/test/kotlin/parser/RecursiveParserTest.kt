package parser

import tokenizer.TokensSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tokenizer.TokenType
import tokenizer.Tokenizer

class RecursiveParserTest {

    @ParameterizedTest
    @ValueSource(strings = arrayOf("binarysearch.javam","binarytree.javam","bubblesort.javam","factorial.javam","linearsearch.javam","linkedlist.javam","quicksort.javam","treevisitor.javam"))
    fun parse(name: String) {
        parseFile(name)
    }

    private fun parseFile(name : String) {
        val code = getFileContent(name)
        val t =
            Tokenizer().getTokens(code).filter { t -> t.type != TokenType.Whitespace && t.type != TokenType.Comment }
                .toTypedArray()
        val ts = TokensSource(t)
        val p = RecursiveParser(ts).parse()
        Assertions.assertEquals(TokenType.EOF, ts.currentToken.type)
    }

    private fun getFileContent(name : String): String {
        return RecursiveParserTest::class.java.classLoader.getResource(name)!!.readText()
    }
}