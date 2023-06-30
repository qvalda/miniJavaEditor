package models

import editor.LineChangeArgs
import editor.TextEditorModel
import tokenizer.Token
import tokenizer.Tokenizer

class TokenizedTextModel(private val textModel: TextEditorModel) {

    var lines: MutableList<Array<Token>>
    private val tokenizer = Tokenizer()

    init {
        lines = textModel.lines.map { l -> tokenizer.getTokens(l) }.toMutableList()

        textModel.onLineDelete += ::onLineDelete
        textModel.onLineModified += ::onLineModified
        textModel.onLineAdd += ::onLineAdd
    }

    private fun onLineDelete(lineChangeArgs: LineChangeArgs) {
        lines.subList(lineChangeArgs.startIndex, lineChangeArgs.startIndex + lineChangeArgs.count).clear()
    }

    private fun onLineModified(lineIndex: Int) {
        lines[lineIndex] = tokenizer.getTokens(textModel.lines[lineIndex])
    }

    private fun onLineAdd(lineIndex: Int) {
        lines.add(lineIndex, tokenizer.getTokens(textModel.lines[lineIndex]))
    }

    fun iterateTokens(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>> {
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
}