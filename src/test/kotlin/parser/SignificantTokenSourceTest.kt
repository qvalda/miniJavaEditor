package parser

import base.TestUtils.createTokenSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import tokenizer.TokenType

class SignificantTokenSourceTest {

    @Test
    fun testSkipsComments() {
        val ts = createTokenSource(TokenType.KeyWordInt, TokenType.Comment, TokenType.NameIdentifier)
        val sts = SignificantTokenSource(ts)
        assertEquals(TokenType.KeyWordInt, sts.currentToken.type)
        sts.accept()
        assertEquals(TokenType.NameIdentifier, sts.currentToken.type)
        assertFalse(sts.isEOF())
    }
}