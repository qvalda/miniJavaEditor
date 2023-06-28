package ruleProviders

import TokenizedTextModel
import tokenizer.Token
import tokenizer.TokenType

class TokenizerFormattingRuleProvider(private val tokenizedModel: TokenizedTextModel) : IFormattingRuleProvider {

    override fun getFormattingRule(lineIndex: Int): Array<FormattingRule> {
        val rules = ArrayList<FormattingRule>()

        fun addRule(token: Token, style: Style) {
            rules.add(FormattingRule(token.beginIndex, token.endIndex, style))
        }

        for (token in tokenizedModel.lines[lineIndex]) {
            if (token.type.isKeyWord()) {
                addRule(token, Style.KeyWord)
            } else if (token.type == TokenType.Comment) {
                addRule(token, Style.Comment)
            } else if (token.type == TokenType.InvalidSyntax) {
                addRule(token, Style.Error)
            } else if (token.type == TokenType.LiteralNumber) {
                addRule(token, Style.Number)
            } else if (token.type == TokenType.LiteralString) {
                addRule(token, Style.String)
            }
        }

        return rules.toTypedArray()
    }
}