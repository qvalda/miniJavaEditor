package tokenizer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import parser.CodeTestBase
import tokenizer.TokenType.*

class TokenizerTest : CodeTestBase() {

    @ParameterizedTest
    @ValueSource(strings = ["binarysearch.javam", "binarytree.javam", "bubblesort.javam", "factorial.javam", "linearsearch.javam", "linkedlist.javam", "quicksort.javam", "treevisitor.javam"])
    fun canTokenizeTestSamplesWithoutErrors(file: String) {
        val input = getFileContent(file)
        val tokens = getTokenizer().getTokens(input)
        assertFalse(tokens.any { t -> t.type == InvalidSyntax })
    }

    @ParameterizedTest
    @ValueSource(strings = ["\r", "\n", "\t", " ", "\r \n \t   "])
    fun skipWhitespaces(input: String) {
        val tokens = getTokenizer().getTokens(input)
        assertEquals(0, tokens.size)
    }

    @Test
    fun bracketTokenized() {
        val input = "{}()[]"
        val tokens = getTokenizer().getTokens(input).map { t -> t.type }
        assertCollectionEquals(listOf(BracketCurlyOpen, BracketCurlyClose, BracketRoundOpen, BracketRoundClose, BracketSquareOpen, BracketSquareClose), tokens)
    }

    @Test
    fun symbolTokenized() {
        val input = ".,;:"
        val tokens = getTokenizer().getTokens(input).map { t -> t.type }
        assertCollectionEquals(listOf(SymbolDot, SymbolComma, SymbolSemicolon, SymbolColon), tokens)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"\"", "\"a\"", "\"abc\"", "\"abc;\""])
    fun canTokenizeLiteralString(input: String) {
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(LiteralString, 0, input.length, input), tokens[0])
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"", "\"abc"])
    fun hasInvalidLiteralString(input: String) {
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(InvalidSyntax, 0, input.length, input), tokens[0])
    }

    @Test
    fun canTokenizeLiteralChar() {
        val input = "'a'"
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(LiteralChar, 0, 3, "'a'"), tokens[0])
    }

    @Test
    fun hasInvalidLiteralChar() {
        val input = "'"
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(InvalidSyntax, 0, 1, "'"), tokens[0])
    }

    @Test
    fun hasInvalidLiteralDoubleChar() {
        val input = "'ab'"
        val tokens = getTokenizer().getTokens(input)
        assertEquals(2, tokens.size)
        assertEquals(Token(InvalidSyntax, 0, 3, "'ab"), tokens[0])
        assertEquals(InvalidSyntax, tokens[1].type)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "123", "123.32", "123f", "123.5f"])
    fun canTokenizeLiteralNumeral(input: String) {
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(LiteralNumber, 0, input.length, input), tokens[0])
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "123", "123.32", "123f", "123.5f"])
    fun canTokenizeLiteralNumeralWithSemicolon(input: String) {
        val tokens = getTokenizer().getTokens("$input;")
        assertEquals(2, tokens.size)
        assertEquals(Token(LiteralNumber, 0, input.length, input), tokens[0])
        assertEquals(SymbolSemicolon, tokens[1].type)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "123", "123.32", "123f", "123.5f"])
    fun hasInvalidLiteralNumeral(input: String) {
        val tokens = getTokenizer().getTokens(input + "a")
        assertEquals(2, tokens.size)
        assertEquals(Token(InvalidSyntax, 0, input.length, input), tokens[0])
        assertEquals(NameIdentifier, tokens[1].type)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "123", "123.32", "123f", "123.5f"])
    fun hasInvalidLiteralNumeralWithSemicolon(input: String) {
        val tokens = getTokenizer().getTokens(input + "a;")
        assertEquals(3, tokens.size)
        assertEquals(Token(InvalidSyntax, 0, input.length, input), tokens[0])
        assertEquals(NameIdentifier, tokens[1].type)
        assertEquals(SymbolSemicolon, tokens[2].type)
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "abc", "abc123", "abc_def"])
    fun canTokenizeNameIdentifier(input: String) {
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(NameIdentifier, 0, input.length, input), tokens[0])
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "abc", "abc123", "abc_def"])
    fun canTokenizeNameIdentifierWithSemicolon(input: String) {
        val tokens = getTokenizer().getTokens("$input;")
        assertEquals(2, tokens.size)
        assertEquals(Token(NameIdentifier, 0, input.length, input), tokens[0])
        assertEquals(SymbolSemicolon, tokens[1].type)
    }

    @ParameterizedTest
    @ValueSource(strings = ["//", "//abc", "//abc;", "//abc //def"])
    fun canTokenizeComment(input: String) {
        val tokens = getTokenizer().getTokens(input)
        assertEquals(1, tokens.size)
        assertEquals(Token(Comment, 0, input.length, input), tokens[0])
    }

    @Test
    fun hasInvalidToken() {
        val input = "abc %;"
        val tokens = getTokenizer().getTokens(input)
        assertEquals(3, tokens.size)
        assertEquals(NameIdentifier, tokens[0].type)
        assertEquals(Token(InvalidSyntax, 4, 5, "%"), tokens[1])
        assertEquals(SymbolSemicolon, tokens[2].type)
    }

    @Test
    fun canTokenizeOperator() {
        val input = "&& <"
        val tokens = getTokenizer().getTokens(input)
        assertEquals(2, tokens.size)
        assertEquals(Token(OperatorAnd, 0, 2), tokens[0])
        assertEquals(Token(OperatorLess, 3, 4), tokens[1])
    }

    @Test
    fun canTokenizeKeyWord() {
        val input = "abc this 1"
        val tokens = getTokenizer().getTokens(input)
        assertEquals(3, tokens.size)
        assertEquals(NameIdentifier, tokens[0].type)
        assertEquals(Token(KeyWordThis, 4, 8), tokens[1])
        assertEquals(LiteralNumber, tokens[2].type)
    }

    private fun getTokenizer(): Tokenizer {
        return Tokenizer()
    }
}