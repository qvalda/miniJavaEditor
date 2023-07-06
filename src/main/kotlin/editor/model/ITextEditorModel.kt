package editor.model

import helpers.Event

interface ITextEditorModel {
    val onModified: Event<Unit>
    val onLineDelete : Event<LineChangeArgs>
    val onLineModified : Event<LineChangeArgs>
    val onLineAdd : Event<LineChangeArgs>
    val onCaretMove : Event<TextEditorCaret>

    fun getLine(lineIndex: Int): String
    fun getLines(): List<String>
    val enterCaret: TextEditorCaret
    val selectionCaret: TextEditorCaret

    val maxLength: Int
    val linesCount: Int
}