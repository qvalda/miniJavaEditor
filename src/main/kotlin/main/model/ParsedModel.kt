package main.model

import helpers.Event
import parser.IParser
import parser.ITokenSource
import parser.ParserResult

class ParsedModel(private val parser: IParser) : IParsedModel {

    override var parserResult: ParserResult? = null
    override val parserResultChanged = Event<Unit>()

    fun rebuild(tokenSource: ITokenSource) {
        parserResult = parser.parse(tokenSource)
        parserResultChanged(Unit)
    }
}