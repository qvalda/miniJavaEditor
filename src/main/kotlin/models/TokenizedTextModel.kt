package models

import editor.model.ITextEditorModel
import editor.model.LineChangeArgs
import helpers.Event
import parser.ITokenSource
import tokenizer.Token
import tokenizer.Tokenizer

class TokenizedTextModel(private val textModel: ITextEditorModel) {
    val modified = Event<Unit>()
    var lines: MutableList<List<Token>>
    private val tokenizer = Tokenizer()

    init {
        lines = textModel.getLines().map { l -> tokenizer.getTokens(l) }.toMutableList()

        textModel.onLineDelete += ::onLineDelete
        textModel.onLineModified += ::onLineModified
        textModel.onLineAdd += ::onLineAdd
    }

    private fun onLineDelete(lineChangeArgs: LineChangeArgs) {
        lines.subList(lineChangeArgs.startIndex, lineChangeArgs.startIndex + lineChangeArgs.count).clear()
        modified(Unit)
    }

    private fun onLineModified(lineChangeArgs: LineChangeArgs) {
        lines[lineChangeArgs.startIndex] = tokenizer.getTokens(textModel.getLine(lineChangeArgs.startIndex))
        modified(Unit)
    }

    private fun onLineAdd(lineChangeArgs: LineChangeArgs) {
        lines.addAll(lineChangeArgs.startIndex,
            textModel.getLines().subList(lineChangeArgs.startIndex, lineChangeArgs.startIndex + lineChangeArgs.count).map { tokenizer.getTokens(it) })

        modified(Unit)
    }

    fun iterateTokens(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>> { //todo remove?
        val sequence = sequence {
            for (index in startTokenIndex + 1 until lines[startIndex].size) {
                yield(Pair(lines[startIndex][index], startIndex))
            }
            for (index in startIndex + 1 until lines.size) {
                for (t in lines[index]) {
                    yield(Pair(t, index))
                }
            }
        }
        return sequence
    }

    fun iterateTokensBackward(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>> {
        val sequence = sequence {
            for (index in startTokenIndex - 1 downTo 0) {
                yield(Pair(lines[startIndex][index], startIndex))
            }
            for (index in startIndex - 1 downTo 0) {
                for (t in lines[index].size -1 downTo 0) {
                    yield(Pair(lines[index][t], index))
                }
            }
        }
        return sequence
    }

    fun asTokenSource(): ITokenSource {
        return TokenizedTextModelTokenSource(this)
    }

    private class TokenizedTextModelTokenSource(private val tokenizedTextModel: TokenizedTextModel) : ITokenSource {
        private class Cursor(val line: Int, val column: Int)

        private var cursor = Cursor(0, 0)

        override val currentToken: Token
            get() {
                if (isEOF()) return Token.EOF
                return tokenizedTextModel.lines[cursor.line][cursor.column]
            }

        override val lineIndex: Int
            get() = cursor.line

        override fun accept() {
            cursor = getNextCursor()
        }

        private fun getNextCursor(): Cursor {
            return if (cursor.column < tokenizedTextModel.lines[cursor.line].size - 1) {
                Cursor(cursor.line, cursor.column + 1)
            } else {
                var newLine = cursor.line
                do {
                    newLine++
                } while (newLine < tokenizedTextModel.lines.size && tokenizedTextModel.lines[newLine].isEmpty())
                Cursor(newLine, 0)
            }
        }

        override fun isEOF() = cursor.line >= tokenizedTextModel.lines.lastIndex && cursor.column >= tokenizedTextModel.lines.last().size
    }
}