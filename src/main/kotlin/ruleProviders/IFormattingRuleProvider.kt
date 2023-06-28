package ruleProviders

import ruleProviders.FormattingRule

interface IFormattingRuleProvider{
    fun getFormattingRule(lineIndex: Int): Array<FormattingRule>
}