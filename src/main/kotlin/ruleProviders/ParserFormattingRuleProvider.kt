package ruleProviders

import editor.view.BaseFormattingRuleProvider
import editor.view.FormattingRule
import editor.view.Style
import models.ParsedTextModel
import models.TokenizedTextModel

class ParserFormattingRuleProvider(private val parserModel: ParsedTextModel, tokenizedTextModel: TokenizedTextModel) : BaseFormattingRuleProvider() {

    private var errors = mapOf<Int, List<FormattingRule>>()

    init {
        onParserResultChanged(Unit)
        parserModel.parserResultChanged += ::onParserResultChanged
        tokenizedTextModel.modified += ::onTokenizedTextModelChanged
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

    private fun onTokenizedTextModelChanged(unit: Unit) {
        errors = emptyMap()
        changed(Unit)
    }

    override fun getRules(lineIndex: Int): List<FormattingRule> {
        return errors[lineIndex] ?: emptyList()
    }
}