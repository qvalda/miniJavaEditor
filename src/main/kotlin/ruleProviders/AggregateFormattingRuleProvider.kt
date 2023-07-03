package ruleProviders

import editor.BaseFormattingRuleProvider
import editor.FormattingRule
import editor.IFormattingRuleProvider

class AggregateFormattingRuleProvider(private vararg val providers: IFormattingRuleProvider) : BaseFormattingRuleProvider() {

    init {
        for (provider in providers) {
            provider.changed += ::onProviderChange
        }
    }

    private fun onProviderChange(unit: Unit) {
        changed(Unit)
    }

    override fun getRules(lineIndex: Int): List<FormattingRule> {
        return providers.flatMap { p -> p.getRules(lineIndex) }
    }
}