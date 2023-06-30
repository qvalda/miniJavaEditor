package ruleProviders

import editor.FormattingRule
import editor.IFormattingRuleProvider

class AggregateFormattingRuleProvider(private vararg val providers: IFormattingRuleProvider) : IFormattingRuleProvider {

    override fun getFormattingRule(lineIndex: Int): List<FormattingRule> {
        return providers.flatMap { p -> p.getFormattingRule(lineIndex) }
    }
}