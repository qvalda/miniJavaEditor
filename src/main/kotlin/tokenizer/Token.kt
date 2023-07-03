package tokenizer

class Token(val type: TokenType, val startIndex : Int, val endIndex : Int, val value: String? = null) {

    override fun toString(): String {
        return "[${startIndex}:${endIndex}]${type}${if (value != null) "=$value" else ""}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (type != other.type) return false
        if (startIndex != other.startIndex) return false
        if (endIndex != other.endIndex) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + startIndex
        result = 31 * result + endIndex
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    companion object{
        val EOF = Token(TokenType.InvalidSyntax, 0,0, "Unexpected EOF")
    }
}