package parser

import tokenizer.Token

interface ITokenSource {
    val currentToken: Token
    val lineIndex: Int
    fun accept()
    fun isEOF(): Boolean
}