package editor.model

import org.junit.jupiter.api.Assertions

open class TextEditorModelBaseTest {

    val clipboard = MemoryClipboard()

    fun creteTextEditorModelWithCaret(text: String): TextEditorModel {
        val model = TextEditorModel(cleanTestText(text), clipboard)
        val caretPos = getCaretPos(text)
        val enterCaretPos = getEnterCaretPos(text)
        val selectionCaretPos = getSelectionCaretPos(text)
        if(enterCaretPos!=null &&selectionCaretPos!=null) {
            model.setCarets(enterCaretPos.first, enterCaretPos.second)
            model.setSelectionCaret(selectionCaretPos.first, selectionCaretPos.second)
        }
        else if(caretPos!=null) {
            model.setCarets(caretPos.first, caretPos.second)
        }
        return model
    }

    private fun getPos(text: String, pattern: String) = text.split("\r\n").mapIndexed { index: Int, s: String -> Pair(index, s.indexOf(pattern)) }.firstOrNull { p -> p.second != -1 }
    private fun getCaretPos(text: String) = getPos(text, "|")
    private fun getEnterCaretPos(text: String) = getPos(text.replace("]", ""), "[")
    private fun getSelectionCaretPos(text: String) = getPos(text.replace("[", ""), "]")

    fun assertEnterCaret(model: TextEditorModel, line: Int, column: Int) {
        Assertions.assertEquals(line, model.enterCaret.line)
        Assertions.assertEquals(column, model.enterCaret.column)
    }

    fun assertTextAndCaret(model: TextEditorModel, text: String) {
        val caretPos = getCaretPos(text)
        val enterCaretPos = getEnterCaretPos(text)
        val selectionCaretPos = getSelectionCaretPos(text)

        if (enterCaretPos != null && selectionCaretPos != null) {
            Assertions.assertEquals(enterCaretPos.first, model.enterCaret.line)
            Assertions.assertEquals(enterCaretPos.second, model.enterCaret.column)
            Assertions.assertEquals(selectionCaretPos.first, model.selectionCaret.line)
            Assertions.assertEquals(selectionCaretPos.second, model.selectionCaret.column)
        } else if (caretPos != null) {
            Assertions.assertEquals(caretPos.first, model.selectionCaret.line)
            Assertions.assertEquals(caretPos.second, model.selectionCaret.column)
            Assertions.assertEquals(caretPos.first, model.selectionCaret.line)
            Assertions.assertEquals(caretPos.second, model.selectionCaret.column)
        }

        Assertions.assertEquals(cleanTestText(text), model.getText())
    }

    private fun cleanTestText(text: String) = text.replace("|", "").replace("[", "").replace("]", "")

    class MemoryClipboard : IClipboard {
        private var memoryData: String? = null
        override fun getData(): String? {
            return memoryData
        }

        override fun setData(text: String) {
            memoryData = text
        }
    }
}
