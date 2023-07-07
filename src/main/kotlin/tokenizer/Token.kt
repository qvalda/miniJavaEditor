package tokenizer

class Token(val type: TokenType, val startIndex : Int, val endIndex : Int, val value: String? = null) {

    override fun toString(): String {
        return "[${startIndex}:${endIndex}]${type}${if (value != null) "=$value" else ""}"
    }

    companion object {
        val EOF = Token(TokenType.InvalidSyntax, 0, 10, "Unexpected EOF")
    }
}