package tokenizer

class TokensSource(private val tokens : Array<Token>) {
    private var index = 0;

    fun accept() {
        index++
    }

    fun isEOF(): Boolean {
        return index >= tokens.size || tokens[index] == Token.EOF
    }

    val currentToken: Token
        get() {
            return tokens[index]
        }
    val nextToken: Token
        get() {
            return tokens[index + 1]
        }
    val nextToken2: Token
        get() {
            return tokens[index + 2]
        }
}