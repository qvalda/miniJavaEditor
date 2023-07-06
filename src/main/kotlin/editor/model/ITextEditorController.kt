package editor.model

interface ITextEditorController {
    val selectionCaret : TextEditorCaret

    fun backSpaceAction()
    fun deleteAction()
    fun addChar(keyChar: Char)
    fun tabAction()
    fun enterAction()
    fun undo()
    fun redo()
    fun selectAllAction()
    fun cutAction()
    fun copyAction()
    fun pasteAction()
    fun homeAction()
    fun endAction()
    fun pageUpAction(offset: Int)
    fun pageDownAction(offset: Int)
    fun moveEnterCaretLeft()
    fun moveEnterCaretUp()
    fun moveEnterCaretRight()
    fun moveEnterCaretDown()
    fun moveSelectionCaretLeft()
    fun moveSelectionCaretUp()
    fun moveSelectionCaretRight()
    fun moveSelectionCaretDown()
    fun setSelectionCaret(lineIndex: Int, columnIndex: Int)
    fun setCarets(lineIndex: Int, columnIndex: Int)
}