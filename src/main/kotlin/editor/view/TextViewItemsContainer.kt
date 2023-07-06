package editor.view

import editor.model.ITextEditorModel
import editor.model.LineChangeArgs
import editor.view.item.Caret
import editor.view.item.IViewItem
import editor.view.item.Selection
import editor.view.item.StringRow
import helpers.Event
import java.awt.Dimension

class TextViewItemsContainer(private val model: ITextEditorModel): IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()

    init {
        model.onModified += ::onModelModified
    }

    private fun onModelModified(args: LineChangeArgs){
        onItemsUpdated(Unit)
    }

    override val size: Dimension
        get() = Dimension(model.maxLength, model.linesCount)

    override fun getItems(lineIndex: Int): List<IViewItem> {
        val mutableList = mutableListOf<IViewItem>()

        val minCaret = minOf(model.enterCaret, model.selectionCaret)
        val maxCaret = maxOf(model.enterCaret, model.selectionCaret)

        if (minCaret == maxCaret || lineIndex < minCaret.line || lineIndex > maxCaret.line) {

        } else if (minCaret.line == maxCaret.line) {
            mutableList.add(Selection(minCaret.column, maxCaret.column))
        } else if (lineIndex == minCaret.line) {
            mutableList.add(Selection(minCaret.column, model.getLine(lineIndex).length))
        } else if (lineIndex == maxCaret.line) {
            mutableList.add(Selection(0, maxCaret.column))
        } else {
            mutableList.add(Selection(0, model.getLine(lineIndex).length))
        }

        mutableList.add(StringRow(model.getLine(lineIndex)))

        if (model.enterCaret.line == lineIndex) {
            mutableList.add(Caret(model.enterCaret))
        }
        if (model.selectionCaret.line == lineIndex && model.selectionCaret != model.enterCaret) {
            mutableList.add(Caret(model.selectionCaret))
        }

        return mutableList
    }
}