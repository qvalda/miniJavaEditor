package main.itemsContainer

import editor.view.IViewItemsContainer
import editor.view.item.ErrorViewItem
import editor.view.item.IViewItem
import helpers.Event
import main.model.IParsedModel
import main.model.ITokenizedModel
import parser.*

class UniqueClassCheckerViewItemsContainer(tokenizedModel: ITokenizedModel, private val parserModel: IParsedModel) : IViewItemsContainer, NodeWithLocationVisitor() {

    override val onItemsUpdated = Event<Unit>()

    private var errors = mutableMapOf<Int, MutableList<ErrorViewItem>>()

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
            errors.clear()
            onItemsUpdated(Unit)
        }
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            parserModel.parserResult?.program?.accept(this)
            onItemsUpdated(Unit)
        }
    }

    private fun mergeErrors(newErrors: Map<Int, MutableList<ErrorViewItem>>) {
        for (e in newErrors) {
            if (errors.containsKey(e.key)) {
                errors[e.key]!!.addAll(e.value)
            } else {
                errors[e.key] = e.value
            }
        }
    }

    override fun visit(node: ProgramNode) {
        super.visit(node)
        mergeErrors(getDuplicates(node.classes))
    }

    override fun visit(node: MainClassNode) {
        super.visit(node)
        mergeErrors(getDuplicates(node.variables))
    }

    override fun visit(node: ClassDeclarationNode) {
        super.visit(node)
        mergeErrors(getDuplicates(node.variables))
        mergeErrors(getDuplicates(node.methods))
    }

    override fun visit(node: MethodDeclarationNode) {
        super.visit(node)
        mergeErrors(getDuplicates(node.variables))
        mergeErrors(getDuplicates(node.arguments))
    }

    private fun getDuplicates(nodes: List<INodeWithLocation>): Map<Int, MutableList<ErrorViewItem>> {
        return nodes
            .groupBy { c -> c.name }
            .filter { g -> g.value.size > 1 }
            .flatMap { c -> c.value }
            .groupBy { e -> e.location.lineIndex }
            .asIterable()
            .associate { g -> g.key to g.value.map { e -> ErrorViewItem("Duplicated declarations", e.location.startIndex, e.location.endIndex) }.toMutableList() }
    }
}

