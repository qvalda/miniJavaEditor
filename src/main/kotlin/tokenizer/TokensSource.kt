package tokenizer

interface ITokenSource {
    val currentToken: Token
    val nextToken: Token
    fun accept()
    fun isEOF(): Boolean
}

class TokensSource(private val tokens : Array<Token>) : ITokenSource {
    private var index = 0

    override fun accept() {
        index++
    }

    override fun isEOF(): Boolean {
        return index >= tokens.size
    }

    override val currentToken: Token
        get() {
            return tokens[index]
        }
    override val nextToken: Token
        get() {
            return tokens[index + 1]
        }
}