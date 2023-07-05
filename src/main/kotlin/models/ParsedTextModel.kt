package models

import helpers.Event
import parser.ITokenSource
import parser.ParserResult
import parser.RecursiveParser

class ParsedTextModel(tokenSource: ITokenSource) {

    var parserResult: ParserResult? = null
    val parserResultChanged = Event<Unit>()

    init {
        parserResult = RecursiveParser(tokenSource).parse()
    }

    fun update(tokenSource: ITokenSource) {
        parserResult = RecursiveParser(tokenSource).parse()
        parserResultChanged(Unit)
    }
}