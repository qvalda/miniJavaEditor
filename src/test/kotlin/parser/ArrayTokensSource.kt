package parser

import tokenizer.Token

class ArrayTokensSource(private val tokens : List<Token>) : ITokenSource {
    private var index = 0

    override val currentToken: Token
        get() = if (isEOF()) Token.EOF else tokens[index]

    override val lineIndex: Int
        get() = 0

    override fun accept() {
        index++
    }

    override fun isEOF(): Boolean = index >= tokens.size
}