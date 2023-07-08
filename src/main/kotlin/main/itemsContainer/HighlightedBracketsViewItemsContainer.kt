package main.itemsContainer

import editor.model.ITextEditorModel
import editor.model.TextEditorCaret
import editor.view.DrawMeasures
import editor.view.IViewItemsContainer
import editor.view.Style
import editor.view.item.IViewItem
import helpers.DrawStateSaver
import helpers.Event
import main.model.ITokenizedModel
import tokenizer.Token
import tokenizer.TokenType
import java.awt.Graphics

class HighlightedBracketsViewItemsContainer(private val textEditorModel: ITextEditorModel, private val tokenizedModel: ITokenizedModel) : IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()
    private var highlightedBrackets = mutableListOf<Token>()

    init {
        textEditorModel.onCaretMove += ::onCaretMove
    }

    private fun onCaretMove(caret: TextEditorCaret) {
        highlightedBrackets.clear()
        val line = tokenizedModel.getLine(caret.line)
        val bracket = line.firstOrNull { t -> t.type.isBracket() && caret.column >= t.startIndex && caret.column <= t.endIndex }
        if (bracket != null) {
            highlightedBrackets.add(bracket)
            val pairBracket = getPairBracket(bracket.type)

            val tokens = when (getLookupDirection(bracket.type)) {
                LookupDirection.Backward -> tokenizedModel.iterateTokensBackward(caret.line, line.indexOf(bracket))
                LookupDirection.Forward -> tokenizedModel.iterateTokens(caret.line, line.indexOf(bracket))
            }

            var skipCount = 0
            for (token in tokens) {
                if (token.first.type == pairBracket) {
                    if (skipCount == 0) {
                        highlightedBrackets.add(token.first)
                        break
                    } else {
                        skipCount--
                    }
                } else if (token.first.type == bracket.type) {
                    skipCount++
                }
            }
        }
    }

    override fun getItems(lineIndex: Int): List<IViewItem> {
        val rules = mutableListOf<ColoredBracket>()

        for (token in tokenizedModel.getLine(lineIndex)) {
            if (token in highlightedBrackets) {
                val text = textEditorModel.getLine(lineIndex).substring(token.startIndex, token.endIndex)
                rules.add(ColoredBracket(text, token.startIndex))
            }
        }

        return rules
    }

    private enum class LookupDirection {
        Backward,
        Forward
    }

    private fun getPairBracket(type: TokenType): TokenType {
        return when (type) {
            TokenType.BracketRoundOpen -> TokenType.BracketRoundClose
            TokenType.BracketRoundClose -> TokenType.BracketRoundOpen
            TokenType.BracketSquareOpen -> TokenType.BracketSquareClose
            TokenType.BracketSquareClose -> TokenType.BracketSquareOpen
            TokenType.BracketCurlyOpen -> TokenType.BracketCurlyClose
            TokenType.BracketCurlyClose -> TokenType.BracketCurlyOpen
            else -> throw IllegalArgumentException()
        }
    }

    private fun getLookupDirection(type: TokenType): LookupDirection {
        return when (type) {
            TokenType.BracketRoundOpen -> LookupDirection.Forward
            TokenType.BracketRoundClose -> LookupDirection.Backward
            TokenType.BracketSquareOpen -> LookupDirection.Forward
            TokenType.BracketSquareClose -> LookupDirection.Backward
            TokenType.BracketCurlyOpen -> LookupDirection.Forward
            TokenType.BracketCurlyClose -> LookupDirection.Backward
            else -> throw IllegalArgumentException()
        }
    }

    private class ColoredBracket(private val text: String, private val column: Int) : IViewItem {
        override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
            val lineY = measures.letterHeight + lineIndex * measures.letterHeight - measures.letterShift
            DrawStateSaver.usingColor(g, Style.Bracket.background!!) {
                g.fillRect(
                    column * measures.letterWidth,
                    lineIndex * measures.letterHeight,
                    measures.letterWidth,
                    measures.letterHeight
                )
            }
            g.drawString(text, column * measures.letterWidth, lineY)
        }
    }
}