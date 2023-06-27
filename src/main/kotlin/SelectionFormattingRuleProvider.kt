class SelectionFormattingRuleProvider(private val textModel: EditorTextModel) : IFormattingRuleProvider {
    override fun getFormattingRule(lineIndex: Int): Array<FormattingRule> {
        val minCaret = minOf(textModel.beginCaret, textModel.endCaret)
        val maxCaret = maxOf(textModel.beginCaret, textModel.endCaret)

        if (minCaret == maxCaret || lineIndex < minCaret.line || lineIndex > maxCaret.line) {
            return emptyArray()
        }

        if (minCaret.line == maxCaret.line) {
            return arrayOf(FormattingRule(minCaret.column, maxCaret.column, Style.Selection))
        }

        if (lineIndex == minCaret.line) {
            return arrayOf(FormattingRule(minCaret.column, textModel.lines[lineIndex].text.length, Style.Selection))
        }

        if (lineIndex == maxCaret.line) {
            return arrayOf(FormattingRule(0, maxCaret.column, Style.Selection))
        }
        return arrayOf(FormattingRule(0, textModel.lines[lineIndex].text.length, Style.Selection))
    }
}