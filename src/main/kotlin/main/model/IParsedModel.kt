package main.model

import helpers.Event
import parser.ParserResult

interface IParsedModel {
    var parserResult: ParserResult?
    val parserResultChanged: Event<Unit>
}