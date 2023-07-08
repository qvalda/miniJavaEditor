package main.itemsContainer

import editor.model.ITextEditorModel
import editor.model.TextEditorCaret
import editor.view.DrawMeasures
import helpers.Event
import main.model.ITokenizedModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import tokenizer.Token
import tokenizer.TokenType
import java.awt.Graphics

class HighlightedBracketsViewItemsContainerTest {

    private val measures = DrawMeasures(1, 1, 1)
    private lateinit var textModel: ITextEditorModel
    private lateinit var tokenizedModel: ITokenizedModel
    private var textModelOnCaretMove = Event<TextEditorCaret>()
    private lateinit var graphics: Graphics

    private val token1 = Pair(Token(TokenType.BracketCurlyOpen, 0, 1), 0)
    private val token2 = Pair(Token(TokenType.BracketRoundOpen, 1, 2), 0)
    private val token3 = Pair(Token(TokenType.BracketRoundOpen, 2, 3), 0)
    private val token4 = Pair(Token(TokenType.BracketRoundClose, 3, 4), 0)
    private val token5 = Pair(Token(TokenType.BracketRoundClose, 4, 5), 0)
    private val token6 = Pair(Token(TokenType.BracketCurlyClose, 0, 1), 1)

    @BeforeEach
    fun recreateModels() {
        graphics = mock(Graphics::class.java)
        textModel = mock(ITextEditorModel::class.java)
        `when`(textModel.onCaretMove).thenReturn(textModelOnCaretMove)
        `when`(textModel.getLine(0)).thenReturn("{(())")
        `when`(textModel.getLine(1)).thenReturn("}")

        tokenizedModel = mock(ITokenizedModel::class.java)
        `when`(tokenizedModel.getLine(0)).thenReturn(listOf(token1, token2, token3, token4, token5).map { t -> t.first })
        `when`(tokenizedModel.getLine(1)).thenReturn(listOf(token6.first))

        `when`(tokenizedModel.iterateTokens(0, 0)).thenReturn(tokenSequence().drop(1))
        `when`(tokenizedModel.iterateTokensBackward(0, 4)).thenReturn(tokenSequence().take(4).toList().reversed().asSequence())
    }

    private fun tokenSequence(): Sequence<Pair<Token, Int>> {
        return sequenceOf(token1, token2, token3, token4, token5, token6)
    }

    @Test
    fun findBracketOnOtherLine() {
        val container = HighlightedBracketsViewItemsContainer(textModel, tokenizedModel)

        textModelOnCaretMove(TextEditorCaret(0, 0))

        val line0Items = container.getItems(0)
        val line1Items = container.getItems(1)

        assertEquals(1, line0Items.size)
        assertEquals(1, line1Items.size)

        line0Items[0].draw(graphics, 0, measures)
        verify(graphics).drawString("{", 0, 0)

        line1Items[0].draw(graphics, 1, measures)
        verify(graphics).drawString("}", 0, 1)
    }

    @Test
    fun findBracketWithSkipAndReverse() {
        val container = HighlightedBracketsViewItemsContainer(textModel, tokenizedModel)

        textModelOnCaretMove(TextEditorCaret(0, 5))

        val line0Items = container.getItems(0)
        val line1Items = container.getItems(1)

        assertEquals(2, line0Items.size)
        assertEquals(0, line1Items.size)

        line0Items[0].draw(graphics, 0, measures)
        verify(graphics).drawString("(", 1, 0)

        line0Items[1].draw(graphics, 0, measures)
        verify(graphics).drawString(")", 4, 0)
    }
}