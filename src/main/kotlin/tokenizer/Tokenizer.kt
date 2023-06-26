package tokenizer

class Tokenizer {

    private val numbers = '0'..'9'
    //private val whiteSpaces = arrayOf('\r', '\n', ' ', '\t')
    private val whiteSpaces = arrayOf(' ', '\t')
    private val numberEndings = arrayOf('f', 'd', 'L')
    private val letters = ('a'..'z').union(('A'..'Z')).union(charArrayOf('_').asIterable()).toSet()
    private val lettersWithNumbers = letters.union(numbers).toSet()
    private val operatorChars = Tokens.operators.keys.flatMap { k-> k.toCharArray().asIterable() }.toSet()

    private fun tryReadWhiteSpaceToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in whiteSpaces) return null

        val begin = reader.pointer;
        while (reader.moveNext()) {
            when (reader.currentChar) {
                in whiteSpaces -> continue
                else -> break
            }
        }

        return Token(TokenType.Whitespace, reader.substring(begin, reader.pointer))
        //return Token(TokenType.Whitespace, begin, reader.pointer)
    }

    private fun tryReadNewLineToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar != '\n') return null
        reader.moveNext()
        return Token(TokenType.NewLine)
    }

    private fun tryReadBracketToken(reader: CharArraySafeReader): Token? {
        if(reader.currentChar !in Tokens.brackets) return null

        val type = Tokens.brackets[reader.currentChar]
        reader.moveNext()

        return Token(type!!)
    }

    private fun tryReadSymbolToken(reader: CharArraySafeReader): Token? {
        if(reader.currentChar !in Tokens.symbols) return null

        val type = Tokens.symbols[reader.currentChar]
        reader.moveNext()

        return Token(type!!)
    }

    private fun tryReadLiteralStringToken(reader: CharArraySafeReader): Token? {
        if(reader.currentChar != '"') return null

        val begin = reader.pointer;
        while (reader.moveNext()) {
            when (reader.currentChar) {
                '"' -> {
                    reader.moveNext()
                    break
                }
                else -> continue
            }
        }

        return Token(TokenType.LiteralString, reader.substring(begin, reader.pointer))
    }

    private fun tryReadLiteralCharToken(reader: CharArraySafeReader): Token? {
        if(reader.currentChar != '\'') return null

        val begin = reader.pointer;
        var state = LiteralCharParserStates.OpenBracket
        while (reader.moveNext()) {
            when (state){
                LiteralCharParserStates.OpenBracket -> state = LiteralCharParserStates.Char
                LiteralCharParserStates.Char -> {
                    if (reader.currentChar == '\''){
                        state = LiteralCharParserStates.CloseBracket
                    }
                    else{
                        return Token(TokenType.InvalidSyntax, reader.substring(begin, reader.pointer))
                    }
                }
                LiteralCharParserStates.CloseBracket -> break
            }
        }

        return Token(TokenType.LiteralChar, reader.substring(begin, reader.pointer))
    }

    private fun tryReadLiteralNumericToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in numbers) return null

        val begin = reader.pointer;
        var state = LiteralNumberParserStates.IntegerPart
        while (reader.moveNext()) {
            when (state) {
                LiteralNumberParserStates.IntegerPart -> {
                    when (reader.currentChar) {
                        in numbers -> state = LiteralNumberParserStates.IntegerPart
                        in numberEndings -> state = LiteralNumberParserStates.LetterEnding
                        '.' -> state = LiteralNumberParserStates.FloatDelimiter
                        else -> break
                    }
                }

                LiteralNumberParserStates.FloatDelimiter -> {
                    when (reader.currentChar) {
                        in numbers -> state = LiteralNumberParserStates.FloatPart
                        else -> return Token(TokenType.InvalidSyntax, reader.substring(begin, reader.pointer))
                    }
                }

                LiteralNumberParserStates.FloatPart -> {
                    when (reader.currentChar) {
                        in numbers -> state = LiteralNumberParserStates.FloatPart
                        in numberEndings -> state = LiteralNumberParserStates.LetterEnding
                        else -> break
                    }
                }

                LiteralNumberParserStates.LetterEnding -> break
            }
        }
        return Token(TokenType.LiteralNumber, reader.substring(begin, reader.pointer))
    }

    private fun tryReadIdentifierToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in letters) return null

        val begin = reader.pointer;
        while (reader.moveNext()) {
            when (reader.currentChar) {
                in lettersWithNumbers -> continue
                else -> break
            }
        }
        val identifier = reader.substring(begin, reader.pointer)

        val keyword = Tokens.keyWords[identifier]

        if (keyword != null) return Token(keyword, identifier)

        val bool = Tokens.booleans[identifier]
        if (bool != null) return Token(bool, identifier)

        return Token(TokenType.NameIdentifier, identifier)
    }

    private fun tryReadCommentToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar != '/' || reader.getNextChar() != '/') return null

        val begin = reader.pointer;
        while (reader.moveNext()) {
            when (reader.currentChar) {
                '\r', '\n' -> break
                else -> continue
            }
        }

        return Token(TokenType.Comment, reader.substring(begin, reader.pointer))
    }

    private fun tryReadOperatorToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in operatorChars) return null

        if (reader.getNextChar() in operatorChars) {
            val doubleCharOperator = String(charArrayOf(reader.currentChar, reader.getNextChar()))
            val operator = Tokens.operators[doubleCharOperator]
            if (operator != null) {
                reader.moveNext()
                reader.moveNext()
                return Token(operator)
            }
        }
        val singleCharOperator = reader.currentChar.toString()

        val operator = Tokens.operators[singleCharOperator]
        if (operator != null) {
            reader.moveNext()
            return Token(operator)
        }
        reader.moveNext()
        return Token(TokenType.InvalidSyntax)
    }

    private fun createUndefinedToken(reader: CharArraySafeReader): Token {
        reader.moveNext()
        return Token(TokenType.NotDefined)
    }

    fun getTokens(input: String): Array<Token> {
        val tokens = ArrayList<Token>()

        val reader = CharArraySafeReader(input)

        while (!reader.isEOF()) {
            val token =
                tryReadNewLineToken(reader) ?:
                tryReadWhiteSpaceToken(reader) ?:
                tryReadBracketToken(reader) ?:
                tryReadSymbolToken(reader) ?:
                tryReadLiteralStringToken(reader) ?:
                tryReadLiteralCharToken(reader) ?:
                tryReadLiteralNumericToken(reader) ?:
                tryReadIdentifierToken(reader) ?:
                tryReadCommentToken(reader) ?:
                tryReadOperatorToken(reader) ?:
                createUndefinedToken(reader)

            tokens.add(token)
        }

        //tokens.add(Token.EOF)

        return tokens.toTypedArray()
    }
}

enum class LiteralNumberParserStates{
    IntegerPart,
    FloatDelimiter,
    FloatPart,
    LetterEnding
}
enum class LiteralCharParserStates{
    OpenBracket,
    Char,
    CloseBracket,
}










