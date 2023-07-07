package tokenizer

interface ITokenizer {
    fun getTokens(input: String): List<Token>
}