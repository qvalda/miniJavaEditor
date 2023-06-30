package ruleProviders

import editor.FormattingRule
import editor.IFormattingRuleProvider

class EmptyFormattingRuleProvider : IFormattingRuleProvider {

    override fun getFormattingRule(lineIndex: Int): List<FormattingRule> {
        return emptyList()
    }
}