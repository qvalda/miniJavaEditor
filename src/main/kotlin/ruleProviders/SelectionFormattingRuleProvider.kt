package ruleProviders

import editor.*

class SelectionFormattingRuleProvider(private val textModel: TextEditorModel) : BaseFormattingRuleProvider() {
    override fun getRules(lineIndex: Int): List<FormattingRule> {
        val minCaret = minOf(textModel.beginCaret, textModel.endCaret)
        val maxCaret = maxOf(textModel.beginCaret, textModel.endCaret)

        if (minCaret == maxCaret || lineIndex < minCaret.line || lineIndex > maxCaret.line) {
            return emptyList()
        }

        if (minCaret.line == maxCaret.line) {
            return listOf(FormattingRule(minCaret.column, maxCaret.column, Style.Selection))
        }

        if (lineIndex == minCaret.line) {
            return listOf(FormattingRule(minCaret.column, textModel.lines[lineIndex].length, Style.Selection))
        }

        if (lineIndex == maxCaret.line) {
            return listOf(FormattingRule(0, maxCaret.column, Style.Selection))
        }
        return listOf(FormattingRule(0, textModel.lines[lineIndex].length, Style.Selection))
    }
}