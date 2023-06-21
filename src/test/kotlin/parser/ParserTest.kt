package parser

import TokensSource
import org.junit.jupiter.api.Test
import tokenizer.TokenType
import tokenizer.Tokenizer

class ParserTest {
    fun runParser(input: String, entryPoint: Grammar) {
        val parser = Parser()
        val tokenizer = Tokenizer()
        val tokens = tokenizer.getTokens(input).filter { t -> t.type != TokenType.Whitespace }.toTypedArray()
        parser.parse(TokensSource(tokens), entryPoint)
    }

    @Test
    fun testSum() {
        runParser("1+1", Grammar.Expression)
    }
}