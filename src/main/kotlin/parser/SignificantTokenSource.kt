package parser

import tokenizer.Token
import tokenizer.TokenType

class SignificantTokenSource(private val ts: ITokenSource) : ITokenSource {

    init {
        skipNonSignificant()
    }

    override val currentToken: Token
        get() = ts.currentToken
    override val lineIndex: Int
        get() = ts.lineIndex

    override fun accept() {
        ts.accept()
        skipNonSignificant()
    }

    override fun isEOF(): Boolean = ts.isEOF()

    private fun skipNonSignificant() {
        while (!isSignificant(ts.currentToken.type)) {
            ts.accept()
        }
    }

    private fun isSignificant(tokenType: TokenType): Boolean {
        return tokenType != TokenType.Comment
    }
}