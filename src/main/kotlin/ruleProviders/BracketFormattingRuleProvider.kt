package ruleProviders

import editor.*
import models.TokenizedTextModel
import tokenizer.Token
import tokenizer.TokenType

class BracketFormattingRuleProvider(textModel: TextEditorModel, private val tokenizedModel: TokenizedTextModel) : BaseFormattingRuleProvider() {

    private var highlightedBrackets = mutableListOf<Token>()

    init {
        textModel.onCaretMove += ::onCaretMove
    }

    private fun onCaretMove(caret: TextEditorCaret) {
        highlightedBrackets.clear()
        val line = tokenizedModel.lines[caret.line]
        val bracket = line.firstOrNull { t -> t.type.isBracket() && caret.column >= t.startIndex && caret.column <= t.endIndex }
        if (bracket != null) {
            highlightedBrackets.add(bracket)
            val pairBracket = getPairBracket(bracket.type)

            val tokens = when (getLookupDirection(bracket.type)) {
                LookupDirection.Backward -> tokenizedModel.iterateTokensBackward(caret.line, line.indexOf(bracket))
                LookupDirection.Forward -> tokenizedModel.iterateTokens(caret.line, line.indexOf(bracket))
            }

            var skipCount = 0
            for (token in tokens) {
                if (token.first.type == pairBracket) {
                    if (skipCount == 0) {
                        highlightedBrackets.add(token.first)
                        break
                    } else {
                        skipCount--
                    }
                } else if (token.first.type == bracket.type) {
                    skipCount++
                }
            }
        }
    }

    override fun getRules(lineIndex: Int): List<FormattingRule> {
        val rules = mutableListOf<FormattingRule>()

        for (token in tokenizedModel.lines[lineIndex]) {
            if (token in highlightedBrackets) {
                rules.add(FormattingRule(token.startIndex, token.endIndex, Style.Bracket))
            }
        }

        return rules
    }

    enum class LookupDirection {
        Backward,
        Forward
    }

    private fun getPairBracket(type: TokenType): TokenType {
        return when (type) {
            TokenType.BracketRoundOpen -> TokenType.BracketRoundClose
            TokenType.BracketRoundClose -> TokenType.BracketRoundOpen
            TokenType.BracketSquareOpen -> TokenType.BracketSquareClose
            TokenType.BracketSquareClose -> TokenType.BracketSquareOpen
            TokenType.BracketCurlyOpen -> TokenType.BracketCurlyClose
            TokenType.BracketCurlyClose -> TokenType.BracketCurlyOpen
            else -> throw IllegalArgumentException()
        }
    }

    private fun getLookupDirection(type: TokenType): LookupDirection {
        return when (type) {
            TokenType.BracketRoundOpen -> LookupDirection.Forward
            TokenType.BracketRoundClose -> LookupDirection.Backward
            TokenType.BracketSquareOpen -> LookupDirection.Forward
            TokenType.BracketSquareClose -> LookupDirection.Backward
            TokenType.BracketCurlyOpen -> LookupDirection.Forward
            TokenType.BracketCurlyClose -> LookupDirection.Backward
            else -> throw IllegalArgumentException()
        }
    }
}