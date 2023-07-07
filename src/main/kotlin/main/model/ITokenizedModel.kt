package main.model

import helpers.Event
import parser.ITokenSource
import tokenizer.Token

interface ITokenizedModel {
    val modified: Event<Unit>
    fun getLine(index: Int): List<Token>
    val linesCount: Int
    fun iterateTokens(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>>
    fun iterateTokensBackward(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>>
    fun asTokenSource(): ITokenSource
}