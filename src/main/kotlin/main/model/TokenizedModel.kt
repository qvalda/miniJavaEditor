package main.model

import editor.model.ITextEditorModel
import editor.model.LineChangeArgs
import helpers.Event
import parser.ITokenSource
import tokenizer.ITokenizer
import tokenizer.Token
import kotlin.math.min

class TokenizedModel(private val model: ITextEditorModel, private val tokenizer: ITokenizer): ITokenizedModel {
    override val modified = Event<Unit>()
    private var lines = model.getLines().map { l -> tokenizer.getTokens(l) }.toMutableList()

    init {
        model.onLineDelete += ::onLineDelete
        model.onLineModified += ::onLineModified
        model.onLineAdd += ::onLineAdd
    }

    override fun getLine(index: Int): List<Token> {
        return lines[index]
    }

    override val linesCount: Int
        get() = lines.size

    private fun onLineDelete(lineChangeArgs: LineChangeArgs) {
        lines.subList(lineChangeArgs.startIndex, lineChangeArgs.startIndex + lineChangeArgs.count).clear()
        modified(Unit)
    }

    private fun onLineModified(lineChangeArgs: LineChangeArgs) {
        lines[lineChangeArgs.startIndex] = tokenizer.getTokens(model.getLine(lineChangeArgs.startIndex))
        modified(Unit)
    }

    private fun onLineAdd(lineChangeArgs: LineChangeArgs) {
        lines.addAll(lineChangeArgs.startIndex,
            model.getLines().subList(lineChangeArgs.startIndex, lineChangeArgs.startIndex + lineChangeArgs.count).map { tokenizer.getTokens(it) })
        modified(Unit)
    }

    override fun iterateTokens(startIndex: Int, startTokenIndex: Int): Sequence<Pair<Token, Int>> {
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

    override fun iterateTokensBackward(startIndex: Int, startTokenIndex: Int): Sequence<Pair<Token, Int>> {
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

    override fun asTokenSource(): ITokenSource {
        return TokenizedTextModelTokenSource(this)
    }

    private class TokenizedTextModelTokenSource(private val tokenizedModel: TokenizedModel): ITokenSource {
        private class Cursor(val line: Int, val column: Int)

        private var cursor = Cursor(0, 0)

        override val currentToken: Token
            get() {
                if (isEOF()) return Token.EOF
                return tokenizedModel.lines[cursor.line][cursor.column]
            }

        override val lineIndex: Int
            get() = min(cursor.line, tokenizedModel.lines.size - 1)

        override fun accept() {
            cursor = getNextCursor()
        }

        private fun getNextCursor(): Cursor {
            return if (cursor.column < tokenizedModel.lines[cursor.line].size - 1) {
                Cursor(cursor.line, cursor.column + 1)
            } else {
                var newLine = cursor.line
                do {
                    newLine++
                } while (newLine < tokenizedModel.lines.size && tokenizedModel.lines[newLine].isEmpty())
                Cursor(newLine, 0)
            }
        }

        override fun isEOF() = cursor.line >= tokenizedModel.lines.size || cursor.column >= tokenizedModel.lines[cursor.line].size
    }
}