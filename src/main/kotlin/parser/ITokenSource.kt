package parser

import helpers.Event
import tokenizer.Token

interface ITokenSource {
    //val modified: Event<Unit>
    val currentToken: Token
    val lineIndex: Int
    fun accept()
    fun isEOF(): Boolean
    //fun reset()
}