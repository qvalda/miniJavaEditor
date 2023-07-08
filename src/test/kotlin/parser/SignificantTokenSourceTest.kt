package parser

import base.BaseTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tokenizer.TokenType

class SignificantTokenSourceTest: BaseTest() {

    @Test
    fun skipsComments() {
        val ts = createTokenSource(TokenType.KeyWordInt, TokenType.Comment, TokenType.NameIdentifier)
        val sts = SignificantTokenSource(ts)
        assertEquals(TokenType.KeyWordInt, sts.currentToken.type)
        sts.accept()
        assertEquals(TokenType.NameIdentifier, sts.currentToken.type)
        assertFalse(sts.isEOF())
    }
}