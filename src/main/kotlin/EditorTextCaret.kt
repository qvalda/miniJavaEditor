data class EditorTextCaret(val line : Int = 0, val column: Int = 0) : Comparable<EditorTextCaret>{
    override fun compareTo(other: EditorTextCaret): Int {
        if (this == other) return 0;
        if (this.line < other.line) return -1
        if (this.line > other.line) return 1
        return this.column.compareTo(other.column)
    }
}