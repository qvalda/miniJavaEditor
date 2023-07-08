package main.itemsContainer

import editor.view.IViewItemsContainer
import editor.view.item.ErrorViewItem
import editor.view.item.IViewItem
import helpers.Event
import main.model.IParsedModel
import main.model.ITokenizedModel

class ParserViewItemsContainer(tokenizedModel: ITokenizedModel, private val parserModel: IParsedModel): IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()
    private var errors = mapOf<Int, List<ErrorViewItem>>()

    init {
        onParserResultChanged(Unit)
        tokenizedModel.modified += ::onTokenizedTextModelChanged
        parserModel.parserResultChanged += ::onParserResultChanged
    }

    override fun getItems(lineIndex: Int): List<IViewItem> {
        return errors[lineIndex] ?: emptyList()
    }

    private fun onTokenizedTextModelChanged(unit: Unit) {
        if (errors.isNotEmpty()) {
            errors = emptyMap()
            onItemsUpdated(Unit)
        }
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            errors = parserModel.parserResult!!.errors
                .groupBy { e -> e.lineIndex }
                .asIterable()
                .associate { g -> g.key to g.value.map { e -> ErrorViewItem(e.message, e.token.startIndex, e.token.endIndex) }.toList() }
            onItemsUpdated(Unit)
        }
    }
}