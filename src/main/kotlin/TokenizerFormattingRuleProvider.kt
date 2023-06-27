import parser.Statement
import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer

class TokenizerFormattingRuleProvider(private val textModel: EditorTextModel) : IFormattingRuleProvider {

    private var lines : MutableList<Array<Token>>
    private val tokenizer = Tokenizer()

    init {
        lines = textModel.lines.map { l-> tokenizer.getTokens(l.text) }.toMutableList()

        textModel.onLineDelete += ::onLineDelete
        textModel.onLineModified += ::onLineModified
        textModel.onLineAdd += ::onLineAdd
    }

    private fun onLineDelete(lineIndex: Int){
        lines.removeAt(lineIndex)
    }

    private fun onLineModified(lineIndex: Int){
        lines[lineIndex] = tokenizer.getTokens(textModel.lines[lineIndex].text)
    }

    private fun onLineAdd(lineIndex: Int){
        lines.add(lineIndex,tokenizer.getTokens(textModel.lines[lineIndex].text))
    }

    fun iterateTokens(startIndex:Int = 0, startTokenIndex:Int = 0): Sequence<Pair<Token, Int>> {
        val sequence = sequence {
            for ((index, l) in lines.withIndex()) {
                for (t in l) {
                    yield(Pair(t, index))
                }
            }
        }
        return sequence
    }

    override fun getFormattingRule(lineIndex: Int):Array<FormattingRule> {
        val rules = ArrayList<FormattingRule>()


        val line = lines[textModel.beginCaret.line]
        val bracket =
            line.firstOrNull { t -> t.type.isBracket() && textModel.beginCaret.column >= t.beginIndex && textModel.beginCaret.column <= t.endIndex }
        if (bracket != null) {
            if (textModel.beginCaret.line == lineIndex) {
                rules.add(FormattingRule(bracket.beginIndex, bracket.endIndex, Style.Bracket))
            }

            val pairBracket = getPairBracket(bracket.type)
            for (t in iterateTokens(textModel.beginCaret.line, line.indexOf(bracket))) {
                if (t.first.type == pairBracket) {
                    if(t.second == lineIndex) {
                        rules.add(FormattingRule(t.first.beginIndex, t.first.endIndex, Style.Bracket))
                    }
                    break
                }
            }
        }


        for (token in lines[lineIndex]){
            if (token.type.isKeyWord()) {
                rules.add(FormattingRule(token.beginIndex, token.endIndex, Style.KeyWord))
            }
            else if(token.type == TokenType.Comment){
                rules.add(FormattingRule(token.beginIndex, token.endIndex, Style.Comment))
            }
            else if(token.type == TokenType.InvalidSyntax){
                rules.add(FormattingRule(token.beginIndex, token.endIndex, Style.Error))
            }
            else if(token.type == TokenType.LiteralNumber){
                rules.add(FormattingRule(token.beginIndex, token.endIndex, Style.Number))
            }
            else if(token.type == TokenType.LiteralString){
                rules.add(FormattingRule(token.beginIndex, token.endIndex, Style.String))
            }
        }

        return rules.toTypedArray()
    }

    private fun getPairBracket(type: TokenType): Any {
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
}