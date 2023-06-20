package tokenizer

class Token(val type: TokenType, val value: String? = null) {
    override fun toString(): String {
        return if (value == null) type.toString() else "${type}:${value}"
    }
    companion object {
        val EOF: Token = Token(TokenType.EOF)
    }
}