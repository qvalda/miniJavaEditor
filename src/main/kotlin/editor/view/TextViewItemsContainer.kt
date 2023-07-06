package editor.view

import editor.model.ITextEditorModel
import editor.view.item.Caret
import editor.view.item.IViewItem
import editor.view.item.Selection
import editor.view.item.StringRow
import helpers.Event
import java.awt.Dimension

class TextViewItemsContainer(private val text: ITextEditorModel): IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()

    init {
        text.onModified += { onItemsUpdated(Unit) }
    }

    override val size: Dimension
        get() = Dimension(text.maxLength, text.linesCount)

    override fun getItems(lineIndex: Int): List<IViewItem> {
        val mutableList = mutableListOf<IViewItem>()

        val minCaret = minOf(text.enterCaret, text.selectionCaret)
        val maxCaret = maxOf(text.enterCaret, text.selectionCaret)

        if (minCaret == maxCaret || lineIndex < minCaret.line || lineIndex > maxCaret.line) {

        } else if (minCaret.line == maxCaret.line) {
            mutableList.add(Selection(minCaret.column, maxCaret.column))
        } else if (lineIndex == minCaret.line) {
            mutableList.add(Selection(minCaret.column, text.getLine(lineIndex).length))
        } else if (lineIndex == maxCaret.line) {
            mutableList.add(Selection(0, maxCaret.column))
        } else {
            mutableList.add(Selection(0, text.getLine(lineIndex).length))
        }

        mutableList.add(StringRow(text.getLine(lineIndex)))

        if (text.enterCaret.line == lineIndex) {
            mutableList.add(Caret(text.enterCaret))
        }
        if (text.selectionCaret.line == lineIndex && text.selectionCaret != text.enterCaret) {
            mutableList.add(Caret(text.selectionCaret))
        }

        return mutableList
    }
}