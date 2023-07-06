package models

import editor.model.ITextEditorModel
import editor.view.IViewItemsContainer
import editor.view.Style
import editor.view.item.ErrorViewItem
import editor.view.item.IViewItem
import helpers.Event
import java.awt.Dimension

class ParserViewItemsContainer(private val parserModel: ParsedTextModel, tokenizedTextModel: TokenizedTextModel) : IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()
    private var errors = mapOf<Int, List<ErrorViewItem>>()

    init {
        onParserResultChanged(Unit)
        parserModel.parserResultChanged += ::onParserResultChanged
        tokenizedTextModel.modified += ::onTokenizedTextModelChanged
    }

    override fun getItems(lineIndex: Int): List<IViewItem> {
        return errors[lineIndex] ?: emptyList()
    }

    private fun onTokenizedTextModelChanged(unit: Unit) {
        errors = emptyMap()
        onItemsUpdated(Unit)
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            errors = parserModel.parserResult!!.errors
                .groupBy { e -> e.lineIndex }
                .asIterable()
                .associate { g -> g.key to g.value.map { e -> ErrorViewItem(e.token.startIndex, e.token.endIndex, Style.Error) }.toList() }
            onItemsUpdated(Unit)
        }
    }
}