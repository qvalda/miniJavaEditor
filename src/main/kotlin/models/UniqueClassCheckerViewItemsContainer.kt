package models

import editor.view.IViewItemsContainer
import editor.view.Style
import editor.view.item.ErrorViewItem
import editor.view.item.IViewItem
import helpers.Event
import parser.IVisitor
import parser.ProgramNode
import java.awt.Dimension

class UniqueClassCheckerViewItemsContainer(private val parserModel: ParsedTextModel, tokenizedTextModel: TokenizedTextModel) : IViewItemsContainer, IVisitor {

    override val onItemsUpdated = Event<Unit>()

    private var errors = mapOf<Int, List<ErrorViewItem>>()

    init {
        onParserResultChanged(Unit)
        parserModel.parserResultChanged += ::onParserResultChanged
        tokenizedTextModel.modified += ::onTokenizedTextModelChanged
    }

    private fun onTokenizedTextModelChanged(unit: Unit) {
        errors = emptyMap()
        onItemsUpdated(Unit)
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            parserModel.parserResult?.program?.accept(this)
            onItemsUpdated(Unit)
        }
    }

    override fun getItems(lineIndex: Int): List<IViewItem> {
        return errors[lineIndex] ?: emptyList()
    }

    override fun visit(node: ProgramNode) {
        val duplicates = node.classes.groupBy { c -> c.name }.filter { g -> g.value.size > 1 }.flatMap { c -> c.value }
        errors = duplicates
            .groupBy { e -> e.location.lineIndex }
            .asIterable()
            .associate { g -> g.key to g.value.map { e -> ErrorViewItem(e.location.startIndex, e.location.endIndex, Style.Error) }.toList() }
    }
}