package models

import helpers.Event
import helpers.ThrottleCall
import parser.ParserResult
import parser.RecursiveParser

class ParsedTextModel(private val tokenSource: TokenizedTextModel) {
    private val parser = RecursiveParser(tokenSource.asTokenSource())

    val onModifiedThrottled = ThrottleCall(500) { onModified() }

    var parserResult: ParserResult? = null
    val parserResultChanged = Event<Unit>()

    init {
        onModifiedThrottled()
        tokenSource.modified += ::onModifiedDelayed
    }

    private fun onModifiedDelayed(unit: Unit) {
        onModifiedThrottled()
    }

    private fun onModified() {
        //tokenSource.reset()
        parserResult = parser.parse()
        parserResultChanged(Unit)
    }
}