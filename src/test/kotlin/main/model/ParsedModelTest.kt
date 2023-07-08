package main.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import parser.IParser
import parser.ITokenSource
import parser.ParserResult

class ParsedModelTest {

    private val tokenSource = mock(ITokenSource::class.java)
    private val parser = mock(IParser::class.java)
    private val result = ParserResult(null, emptyList())

    init {
        `when`(parser.parse(tokenSource)).thenReturn(result)
    }

    @Test
    fun getParserResult() {
        val model = ParsedModel(parser)
        model.rebuild(tokenSource)

        assertEquals(result, model.parserResult)
    }

    @Test
    fun callsParserResultChanged() {
        val model = ParsedModel(parser)
        var calls = 0
        model.parserResultChanged += { calls++ }

        model.rebuild(tokenSource)
        model.rebuild(tokenSource)

        assertEquals(2, calls)
    }
}