package ruleProviders

import editor.view.BaseFormattingRuleProvider
import editor.view.FormattingRule
import editor.view.IFormattingRuleProvider

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