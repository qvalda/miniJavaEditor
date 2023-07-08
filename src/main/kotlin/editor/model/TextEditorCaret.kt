package editor.model

data class TextEditorCaret(val line: Int = 0, val column: Int = 0): Comparable<TextEditorCaret>{
    override fun compareTo(other: TextEditorCaret): Int {
        if (this == other) return 0
        if (this.line < other.line) return -1
        if (this.line > other.line) return 1
        return this.column.compareTo(other.column)
    }
}