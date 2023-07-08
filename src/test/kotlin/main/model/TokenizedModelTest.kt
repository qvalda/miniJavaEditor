package main.model

import base.BaseTest.Companion.assertCollectionEquals
import mocks.TextEditorModelMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import tokenizer.ITokenizer
import tokenizer.Token
import tokenizer.TokenType

class TokenizedModelTest {

    private val token1 = Token(TokenType.NameIdentifier, 0, 0)
    private val token2 = Token(TokenType.NameIdentifier, 0, 0)
    private val token3 = Token(TokenType.NameIdentifier, 0, 0)
    private val token4 = Token(TokenType.NameIdentifier, 0, 0)

    @Test
    fun tokenizeOnInit() {
        val textModel = TextEditorModelMock("a")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)

        assertEquals(1, tm.getLine(0).size)
        assertEquals(token1, tm.getLine(0)[0])
        assertEquals(1, tm.linesCount)
    }

    @Test
    fun trackLineAdd() {
        val textModel = TextEditorModelMock("a")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)
        var called = false
        tm.modified += { called = true }

        textModel.addLine("b")

        assertTrue(called)
        assertEquals(1, tm.getLine(1).size)
        assertEquals(token2, tm.getLine(1)[0])
        assertEquals(2, tm.linesCount)
    }

    @Test
    fun trackLineModified() {
        val textModel = TextEditorModelMock("a\r\nb")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)
        var called = false
        tm.modified += { called = true }

        assertEquals(1, tm.getLine(1).size)
        assertEquals(token2, tm.getLine(1)[0])
        assertEquals(2, tm.linesCount)

        textModel.modifyLine("c", 1)

        assertTrue(called)
        assertEquals(1, tm.getLine(1).size)
        assertEquals(token3, tm.getLine(1)[0])
        assertEquals(2, tm.linesCount)
    }

    @Test
    fun trackLineDeleted() {
        val textModel = TextEditorModelMock("a\r\nb")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)
        var called = false
        tm.modified += { called = true }

        assertEquals(2, tm.linesCount)

        textModel.deleteLine(1)

        assertTrue(called)
        assertEquals(1, tm.linesCount)
    }

    @Test
    fun iterateTokens() {
        val textModel = TextEditorModelMock("a\r\nb\r\nc\r\nd")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)

        val tokens = tm.iterateTokens(1, 0).toList()
        assertCollectionEquals(listOf(token3, token4), tokens.map { t -> t.first })
        assertCollectionEquals(listOf(2, 3), tokens.map { t -> t.second })
    }

    @Test
    fun iterateTokensBackward() {
        val textModel = TextEditorModelMock("a\r\nb\r\nc\r\nd")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)

        val tokens = tm.iterateTokensBackward(2, 0).toList()
        assertCollectionEquals(listOf(token2, token1), tokens.map { t -> t.first })
        assertCollectionEquals(listOf(1, 0), tokens.map { t -> t.second })
    }

    @Test
    fun asTokenSource() {
        val textModel = TextEditorModelMock("a\r\nb\r\nc\r\nd")
        val tokenizer = createTokenizer()
        val tm = TokenizedModel(textModel, tokenizer)

        val ts = tm.asTokenSource()
        val tokens = mutableListOf<Token>()
        do {
            tokens.add(ts.currentToken)
            ts.accept()
        } while (!ts.isEOF())
        assertCollectionEquals(listOf(token1, token2, token3, token4), tokens)
    }

    private fun createTokenizer(): ITokenizer {
        val tokenizer = Mockito.mock(ITokenizer::class.java)
        Mockito.`when`(tokenizer.getTokens("a")).thenReturn(listOf(token1))
        Mockito.`when`(tokenizer.getTokens("b")).thenReturn(listOf(token2))
        Mockito.`when`(tokenizer.getTokens("c")).thenReturn(listOf(token3))
        Mockito.`when`(tokenizer.getTokens("d")).thenReturn(listOf(token4))
        return tokenizer
    }
}