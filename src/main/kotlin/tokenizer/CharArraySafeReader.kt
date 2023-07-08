package tokenizer

class CharArraySafeReader(private val input: String) {
    var pointer = 0

    val currentChar: Char
        get() = if (pointer < input.length) input[pointer] else Char.MIN_VALUE

    val nextChar: Char
        get() = if (pointer + 1 < input.length) input[pointer + 1] else Char.MIN_VALUE

    fun moveNext(): Boolean {
        pointer++
        return pointer < input.length
    }

    fun isEOF(): Boolean {
        return pointer >= input.length
    }

    fun substring(begin: Int, end: Int): String = input.substring(begin, end)
}