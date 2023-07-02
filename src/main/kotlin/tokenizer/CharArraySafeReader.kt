package tokenizer

class CharArraySafeReader(private val input: String) {
    var pointer = 0
    var line = 0
    var position = 0

    val currentChar: Char
        get() = if (pointer >= input.length) Char.MIN_VALUE else input[pointer]

    fun getNextChar(): Char {
        if (pointer + 1 >= input.length) {
            return Char.MIN_VALUE;
        }
        return input[pointer + 1];
    }

    fun moveNext(): Boolean {
        pointer++;
        return pointer < input.length
    }

    fun isEOF(): Boolean {
        return pointer >= input.length
    }

    fun substring(begin: Int, end: Int): String = input.substring(begin, end)
}