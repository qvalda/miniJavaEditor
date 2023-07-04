package ruleProviders

import editor.BaseFormattingRuleProvider
import editor.FormattingRule
import editor.Style
import models.ParsedTextModel
import parser.IVisitor
import parser.ProgramNode

class UniqueClassNameVisitor(private val parserModel: ParsedTextModel) : IVisitor, BaseFormattingRuleProvider() {

    private var errors = mapOf<Int, List<FormattingRule>>()

    init {
        parserModel.parserResultChanged += ::onParserResultChanged
    }

    private fun onParserResultChanged(unit: Unit) {
        if (parserModel.parserResult != null) {
            parserModel.parserResult?.program?.accept(this)
            changed(Unit)
        }
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