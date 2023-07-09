package main.itemsContainer

import editor.model.ITextEditorModel
import main.model.ITokenizedModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import tokenizer.Token
import tokenizer.TokenType

class TokenizerViewItemsContainerTest {

    private lateinit var textModel: ITextEditorModel
    private lateinit var tokenizedModel: ITokenizedModel

    @BeforeEach
    fun recreateModels() {
        textModel = mock(ITextEditorModel::class.java)
        `when`(textModel.getLine(0)).thenReturn("abc")
        `when`(textModel.getLine(1)).thenReturn("def")

        tokenizedModel = mock(ITokenizedModel::class.java)
        `when`(tokenizedModel.getLine(0)).thenReturn(
            listOf(
                Token(TokenType.Comment, 0, 1),
                Token(TokenType.LiteralChar, 1, 2),
            )
        )
        `when`(tokenizedModel.getLine(1)).thenReturn(
            listOf(Token(TokenType.InvalidSyntax, 0, 1))
        )
    }

    @Test
    fun testGetItems() {
        val model = TokenizerViewItemsContainer(textModel, tokenizedModel)

        assertEquals(2, model.getItems(0).size)
        assertEquals(1, model.getItems(1).size)
    }
}