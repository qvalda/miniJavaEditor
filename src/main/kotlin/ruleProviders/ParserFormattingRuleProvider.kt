package ruleProviders

import editor.BaseFormattingRuleProvider
import editor.FormattingRule
import editor.Style
import models.ParsedTextModel

class ParserFormattingRuleProvider(private val parserModel: ParsedTextModel) : BaseFormattingRuleProvider() {

    private var errors = mapOf<Int, List<FormattingRule>>()

    init {
        parserModel.parserResultChanged += ::onParserResultChanged
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            errors = parserModel.parserResult!!.errors
                .groupBy { e -> e.lineIndex }
                .asIterable()
                .associate { g -> g.key to g.value.map { e -> FormattingRule(e.token.startIndex, e.token.endIndex, Style.Error) }.toList() }
            changed(Unit)
        }
    }

    override fun getRules(lineIndex: Int): List<FormattingRule> {
        return errors[lineIndex] ?: emptyList()
    }
}