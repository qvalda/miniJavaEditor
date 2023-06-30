package editor

interface IFormattingRuleProvider{
    fun getFormattingRule(lineIndex: Int): List<FormattingRule>
}