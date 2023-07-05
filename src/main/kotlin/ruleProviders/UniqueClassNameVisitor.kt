package ruleProviders

import editor.view.BaseFormattingRuleProvider
import editor.view.FormattingRule
import editor.view.Style
import models.ParsedTextModel
import models.TokenizedTextModel
import parser.IVisitor
import parser.ProgramNode

class UniqueClassNameVisitor(private val parserModel: ParsedTextModel, tokenizedTextModel: TokenizedTextModel) : IVisitor, BaseFormattingRuleProvider() {

    private var errors = mapOf<Int, List<FormattingRule>>()

    init {
        onParserResultChanged(Unit)
        parserModel.parserResultChanged += ::onParserResultChanged
        tokenizedTextModel.modified += ::onTokenizedTextModelChanged
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            parserModel.parserResult?.program?.accept(this)
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

    override fun visit(node: ProgramNode) {
        val duplicates = node.classes.groupBy { c -> c.name }.filter { g -> g.value.size > 1 }.flatMap { c -> c.value }
        errors = duplicates
            .groupBy { e -> e.location.lineIndex }
            .asIterable()
            .associate { g -> g.key to g.value.map { e -> FormattingRule(e.location.startIndex, e.location.endIndex, Style.Error) }.toList() }
    }
}