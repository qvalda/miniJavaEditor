package main.itemsContainer

import editor.model.ITextEditorModel
import editor.view.IViewItemsContainer
import editor.view.Style
import editor.view.item.ColoredString
import editor.view.item.ErrorViewItem
import editor.view.item.IViewItem
import helpers.Event
import main.model.ITokenizedModel
import tokenizer.Token
import tokenizer.TokenType

class TokenizerViewItemsContainer(private val textEditorModel: ITextEditorModel, private val tokenizedModel: ITokenizedModel) : IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()

    override fun getItems(lineIndex: Int): List<IViewItem> {
        val items = mutableListOf<IViewItem>()

        fun addColoredString(token: Token, style: Style) {
            val text = textEditorModel.getLine(lineIndex).substring(token.startIndex, token.endIndex)
            items.add(ColoredString(text, token.startIndex, style))
        }

        for (token in tokenizedModel.getLine(lineIndex)) {
            if (token.type.isKeyWord()) {
                addColoredString(token, Style.KeyWord)
            } else if (token.type == TokenType.Comment) {
                addColoredString(token, Style.Comment)
            } else if (token.type == TokenType.InvalidSyntax) {
                items.add(ErrorViewItem("Invalid Syntax", token.startIndex, token.endIndex))
            } else if (token.type == TokenType.LiteralNumber) {
                addColoredString(token, Style.Number)
            } else if (token.type == TokenType.LiteralString) {
                addColoredString(token, Style.String)
            } else if (token.type == TokenType.LiteralChar) {
                addColoredString(token, Style.Char)
            }
        }
        return items
    }
}