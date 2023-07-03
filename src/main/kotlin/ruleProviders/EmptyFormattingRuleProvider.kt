package ruleProviders

import editor.BaseFormattingRuleProvider
import editor.FormattingRule
import editor.IFormattingRuleProvider

class EmptyFormattingRuleProvider : BaseFormattingRuleProvider() {

    override fun getRules(lineIndex: Int): List<FormattingRule> {
        return emptyList()
    }
}