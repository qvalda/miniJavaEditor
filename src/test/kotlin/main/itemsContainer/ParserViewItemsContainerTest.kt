package main.itemsContainer

import helpers.Event
import main.model.IParsedModel
import main.model.ITokenizedModel
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import parser.ParseError
import parser.ParserResult
import tokenizer.Token
import tokenizer.TokenType

class ParserViewItemsContainerTest {

    private val error1 = ParseError(1, Token(TokenType.NameIdentifier, 1,2), "error1")
    private val error2 = ParseError(1, Token(TokenType.NameIdentifier, 3,4), "error2")
    private val resultWithErrors = ParserResult(null, listOf(error1, error2))
    private val resultWithNoErrors = ParserResult(null, emptyList())

    private var tokenizedModelModified = Event<Unit>()
    private var parsedModelResultChanged = Event<Unit>()
    private lateinit var tokenizedModel: ITokenizedModel
    private lateinit var parsedModel: IParsedModel

    @BeforeEach
    fun recreateModels(){
        tokenizedModel = mock(ITokenizedModel::class.java)
        `when`(tokenizedModel.modified).thenReturn(tokenizedModelModified)
        parsedModel = mock(IParsedModel::class.java)
        `when`(parsedModel.parserResultChanged).thenReturn(parsedModelResultChanged)
    }

    @Test
    fun findErrors() {
        `when`(parsedModel.parserResult).thenReturn(resultWithErrors)
        val model = ParserViewItemsContainer(tokenizedModel, parsedModel)

        assertEquals(2, model.getItems(1).size)
    }

    @Test
    fun clearErrorsOnTokenizerModified() {
        `when`(parsedModel.parserResult).thenReturn(resultWithErrors)
        val model = ParserViewItemsContainer(tokenizedModel, parsedModel)

        tokenizedModelModified(Unit)

        assertEquals(0, model.getItems(1).size)
    }

    @Test
    fun trackParserResultChanged() {
        `when`(parsedModel.parserResult).thenReturn(resultWithNoErrors)
        val model = ParserViewItemsContainer(tokenizedModel, parsedModel)

        assertEquals(0, model.getItems(1).size)

        `when`(parsedModel.parserResult).thenReturn(resultWithErrors)
        parsedModelResultChanged(Unit)

        assertEquals(2, model.getItems(1).size)
    }
}