package models

import editor.model.ITextEditorModel
import editor.view.DrawMeasures
import editor.view.IViewItemsContainer
import editor.view.Style
import editor.view.item.ErrorViewItem
import editor.view.item.IViewItem
import helpers.DrawStateSaver
import helpers.Event
import tokenizer.Token
import tokenizer.TokenType
import java.awt.Graphics

class TokenizerViewItemsContainer(private val tokenizedModel: TokenizedTextModel, private val textModel: ITextEditorModel) : IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()

    override fun getItems(lineIndex: Int): List<IViewItem> {
        val rules = mutableListOf<IViewItem>()

        fun addColoredString(token: Token, style: Style) {
            val text = textModel.getLine(lineIndex).substring(token.startIndex, token.endIndex)
            rules.add(ColoredString(text, token.startIndex, style))
        }

        for (token in tokenizedModel.lines[lineIndex]) {
            if (token.type.isKeyWord()) {
                addColoredString(token, Style.KeyWord)
            } else if (token.type == TokenType.Comment) {
                addColoredString(token, Style.Comment)
            } else if (token.type == TokenType.InvalidSyntax) {
                rules.add(ErrorViewItem(token.startIndex, token.endIndex, Style.Error))
            } else if (token.type == TokenType.LiteralNumber) {
                addColoredString(token, Style.Number)
            } else if (token.type == TokenType.LiteralString) {
                addColoredString(token, Style.String)
            } else if (token.type == TokenType.LiteralChar) {
                addColoredString(token, Style.Char)
            }
        }
        return rules
    }

    class ColoredString(private val text: String, private val column: Int, private val style: Style) : IViewItem {
        override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
            val lineY = measures.letterHeight + lineIndex * measures.letterHeight - measures.letterShift
            DrawStateSaver.usingColor(g, style.color!!) {
                DrawStateSaver.usingBold(g, style.isBold)
                {
                    g.drawString(text, column * measures.letterWidth, lineY)
                }
            }
        }
    }
}