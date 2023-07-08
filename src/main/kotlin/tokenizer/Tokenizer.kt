package tokenizer

class Tokenizer : ITokenizer {

    private val numbers = '0'..'9'
    private val whiteSpaces = arrayOf('\r', '\n', ' ', '\t')
    private val numberEndings = arrayOf('f', 'd', 'L')
    private val letters = ('a'..'z').union(('A'..'Z')).union(charArrayOf('_').asIterable()).toSet()
    private val lettersWithNumbers = letters.union(numbers).toSet()
    private val operatorChars = Tokens.operators.keys.flatMap { k -> k.toCharArray().asIterable() }.toSet()

    override fun getTokens(input: String): List<Token> {
        val tokens = mutableListOf<Token>()

        val reader = CharArraySafeReader(input)

        while (!reader.isEOF()) {
            val token =
                tryReadNewLineToken(reader) ?: tryReadWhiteSpaceToken(reader) ?: tryReadBracketToken(reader) ?: tryReadSymbolToken(reader) ?: tryReadLiteralStringToken(reader)
                ?: tryReadLiteralCharToken(reader) ?: tryReadLiteralNumericToken(reader) ?: tryReadIdentifierToken(reader) ?: tryReadCommentToken(reader) ?: tryReadOperatorToken(reader)
                ?: readInvalidToken(reader)

            if (token != null) {
                tokens.add(token)
            }
        }

        return tokens
    }

    private fun tryReadWhiteSpaceToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in whiteSpaces) return null

        val begin = reader.pointer
        while (reader.moveNext()) {
            when (reader.currentChar) {
                in whiteSpaces -> continue
                else -> break
            }
        }

        return null
    }

    private fun tryReadNewLineToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar != '\n') return null
        val begin = reader.pointer
        reader.moveNext()
        return null
    }

    private fun tryReadBracketToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in Tokens.brackets) return null
        val begin = reader.pointer
        val type = Tokens.brackets[reader.currentChar]
        reader.moveNext()

        return Token(type!!, begin, begin + 1)
    }

    private fun tryReadSymbolToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in Tokens.symbols) return null
        val begin = reader.pointer
        val type = Tokens.symbols[reader.currentChar]
        reader.moveNext()

        return Token(type!!, begin, begin + 1)
    }

    private fun tryReadLiteralStringToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar != '"') return null

        val begin = reader.pointer
        var state = LiteralStringParserState.Any
        while (reader.moveNext()) {
            when (state) {
                LiteralStringParserState.Any -> {
                    state = when (reader.currentChar) {
                        '\\' -> LiteralStringParserState.Escaped
                        '"' -> LiteralStringParserState.End
                        else -> LiteralStringParserState.Any
                    }
                }

                LiteralStringParserState.Escaped -> {
                    state = LiteralStringParserState.Any
                }

                LiteralStringParserState.End -> {
                    break
                }
            }
        }
        if (state == LiteralStringParserState.End) {
            return Token(TokenType.LiteralString, begin, reader.pointer, reader.substring(begin, reader.pointer))
        }
        return createInvalidToken(begin, reader)
    }

    private fun tryReadLiteralCharToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar != '\'') return null

        val begin = reader.pointer
        var state = LiteralCharParserStates.Start
        while (reader.moveNext()) {
            when (state) {
                LiteralCharParserStates.Start -> {
                    state = when (reader.currentChar) {
                        '\\' -> LiteralCharParserStates.Escape
                        else -> LiteralCharParserStates.Char
                    }
                }

                LiteralCharParserStates.Escape -> {
                    state = LiteralCharParserStates.Char
                }

                LiteralCharParserStates.Char -> {
                    state = when (reader.currentChar) {
                        '\'' -> LiteralCharParserStates.End
                        else -> break
                    }
                }

                LiteralCharParserStates.End -> break
            }
        }

        if (state == LiteralCharParserStates.End) {
            return Token(TokenType.LiteralChar, begin, reader.pointer, reader.substring(begin, reader.pointer))
        }
        return createInvalidToken(begin, reader)
    }

    private fun tryReadLiteralNumericToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in numbers) return null

        val begin = reader.pointer
        var state = LiteralNumberParserStates.IntegerPart
        while (reader.moveNext()) {
            when (state) {
                LiteralNumberParserStates.IntegerPart -> {
                    when (reader.currentChar) {
                        in numbers -> state = LiteralNumberParserStates.IntegerPart
                        in numberEndings -> state = LiteralNumberParserStates.LetterEnding
                        '.' -> state = LiteralNumberParserStates.FloatDelimiter
                        in lettersWithNumbers -> {
                            state = LiteralNumberParserStates.Error
                            break
                        }

                        else -> break
                    }
                }

                LiteralNumberParserStates.FloatDelimiter -> {
                    when (reader.currentChar) {
                        in numbers -> state = LiteralNumberParserStates.FloatPart
                        else -> {
                            state = LiteralNumberParserStates.Error
                            break
                        }
                    }
                }

                LiteralNumberParserStates.FloatPart -> {
                    when (reader.currentChar) {
                        in numbers -> state = LiteralNumberParserStates.FloatPart
                        in numberEndings -> state = LiteralNumberParserStates.LetterEnding
                        in lettersWithNumbers -> {
                            state = LiteralNumberParserStates.Error
                            break
                        }

                        else -> break
                    }
                }

                LiteralNumberParserStates.LetterEnding -> {
                    when (reader.currentChar) {
                        in lettersWithNumbers -> {
                            state = LiteralNumberParserStates.Error
                            break
                        }

                        else -> break
                    }
                }

                LiteralNumberParserStates.Error -> break
            }
        }

        if (state != LiteralNumberParserStates.Error) {
            return Token(TokenType.LiteralNumber, begin, reader.pointer, reader.substring(begin, reader.pointer))
        }
        return createInvalidToken(begin, reader)
    }

    private fun tryReadIdentifierToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in letters) return null

        val begin = reader.pointer
        while (reader.moveNext()) {
            when (reader.currentChar) {
                in lettersWithNumbers -> continue
                else -> break
            }
        }
        val identifier = reader.substring(begin, reader.pointer)

        val keyword = Tokens.keyWords[identifier]
        if (keyword != null) return Token(keyword, begin, reader.pointer)

        val bool = Tokens.booleans[identifier]
        if (bool != null) return Token(bool, begin, reader.pointer)

        return Token(TokenType.NameIdentifier, begin, reader.pointer, identifier)
    }

    private fun tryReadCommentToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar != '/' || reader.nextChar != '/') return null

        val begin = reader.pointer
        while (reader.moveNext()) {
            when (reader.currentChar) {
                '\r', '\n' -> break
                else -> continue
            }
        }

        return Token(TokenType.Comment, begin, reader.pointer, reader.substring(begin, reader.pointer))
    }

    private fun tryReadOperatorToken(reader: CharArraySafeReader): Token? {
        if (reader.currentChar !in operatorChars) return null
        val begin = reader.pointer
        if (reader.nextChar in operatorChars) {
            val doubleCharOperator = String(charArrayOf(reader.currentChar, reader.nextChar))
            val operator = Tokens.operators[doubleCharOperator]
            if (operator != null) {
                reader.moveNext()
                reader.moveNext()
                return Token(operator, begin, begin + 2)
            }
        }
        val singleCharOperator = reader.currentChar.toString()

        val operator = Tokens.operators[singleCharOperator]
        if (operator != null) {
            reader.moveNext()
            return Token(operator, begin, begin + 1)
        }
        reader.moveNext()
        return createInvalidToken(begin, reader)
    }

    private fun readInvalidToken(reader: CharArraySafeReader): Token? {
        val begin = reader.pointer
        if (reader.isEOF()) {
            return null
        }
        reader.moveNext()
        return createInvalidToken(begin, reader)
    }

    private fun createInvalidToken(begin: Int, reader: CharArraySafeReader): Token {
        return Token(TokenType.InvalidSyntax, begin, reader.pointer, reader.substring(begin, reader.pointer))
    }

    private enum class LiteralNumberParserStates {
        IntegerPart,
        FloatDelimiter,
        FloatPart,
        LetterEnding,
        Error,
    }

    private enum class LiteralCharParserStates {
        Start,
        Escape,
        Char,
        End
    }

    private enum class LiteralStringParserState {
        Any,
        Escaped,
        End
    }
}