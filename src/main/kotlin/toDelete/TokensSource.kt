package toDelete

import helpers.Event
import parser.ITokenSource
import tokenizer.Token

class TokensSource(private val tokens : Array<Token>) : ITokenSource {
    private var index = 0

    override fun accept() {
        index++
    }

    override fun isEOF(): Boolean {
        return index >= tokens.size
    }

    override fun reset() {
        index = 0
    }

    //override val modified = Event<Unit>()

    override val currentToken: Token
        get() {
            return tokens[index]
        }
    override val nextToken: Token
        get() {
            return tokens[index + 1]
        }
    override val lineIndex: Int
        get() { return 0 }
}