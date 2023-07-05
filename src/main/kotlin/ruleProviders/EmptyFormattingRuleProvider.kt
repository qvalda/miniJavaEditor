package ruleProviders

import editor.view.BaseFormattingRuleProvider
import editor.view.FormattingRule

class EmptyFormattingRuleProvider : BaseFormattingRuleProvider() {

    override fun getRules(lineIndex: Int): List<FormattingRule> {
        return emptyList()
    }
}