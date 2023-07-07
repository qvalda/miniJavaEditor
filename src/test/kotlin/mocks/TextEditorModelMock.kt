package mocks

import editor.model.ITextEditorModel
import editor.model.LineChangeArgs
import editor.model.TextEditorCaret
import editor.model.TextEditorModelBaseTest
import helpers.Event

class TextEditorModelMock(text: String) : ITextEditorModel {
    override val onModified = Event<LineChangeArgs>()
    override val onLineDelete = Event<LineChangeArgs>()
    override val onLineModified = Event<LineChangeArgs>()
    override val onLineAdd = Event<LineChangeArgs>()
    override val onCaretMove = Event<TextEditorCaret>()

    override var enterCaret = TextEditorCaret()
    override var selectionCaret = TextEditorCaret()
    override var maxLength: Int = 0
    override var linesCount: Int = 0

    val innerLines: MutableList<String>

    init {
        innerLines = TextEditorModelBaseTest.cleanTestText(text).split("\r\n").toMutableList()

        val caretPos = TextEditorModelBaseTest.getCaretPos(text)
        val enterCaretPos = TextEditorModelBaseTest.getEnterCaretPos(text)
        val selectionCaretPos = TextEditorModelBaseTest.getSelectionCaretPos(text)

        if (enterCaretPos != null && selectionCaretPos != null) {
            enterCaret = TextEditorCaret(enterCaretPos.first, enterCaretPos.second)
            selectionCaret = TextEditorCaret(selectionCaretPos.first, selectionCaretPos.second)
        } else if (caretPos != null) {
            enterCaret = TextEditorCaret(caretPos.first, caretPos.second)
            selectionCaret = enterCaret.copy()
        }
        linesCount = innerLines.size
        maxLength = innerLines.maxByOrNull { l -> l.length }?.length ?: 0
    }

    override fun getLine(lineIndex: Int): String {
        return innerLines[lineIndex]
    }

    override fun getLines(): List<String> {
        return innerLines
    }

    fun addLine(text:String){
        innerLines.add(text)
        onLineAdd(LineChangeArgs(innerLines.size-1))
    }

    fun modifyLine(text:String, index:Int){
        innerLines[index] = text
        onLineModified(LineChangeArgs(index))
    }

    fun deleteLine(index:Int){
        innerLines.removeAt(index)
        onLineDelete(LineChangeArgs(index))
    }

    fun invokeOnModified() {
        onModified(LineChangeArgs(0))
    }
}