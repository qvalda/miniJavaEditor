package ruleProviders

class AggregateFormattingRuleProvider(private val providers: Array<IFormattingRuleProvider>) : IFormattingRuleProvider {

    override fun getFormattingRule(lineIndex: Int): Array<FormattingRule> {
        return providers.flatMap { p -> p.getFormattingRule(lineIndex).asIterable() }.toTypedArray()
    }
}