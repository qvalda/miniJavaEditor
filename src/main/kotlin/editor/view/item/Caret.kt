package editor.view.item

import editor.model.TextEditorCaret
import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver.usingColor
import helpers.DrawStateSaver.usingStroke
import java.awt.Graphics

class Caret(private val caret: TextEditorCaret) : IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        usingColor(g, Style.Caret.color!!) {
            usingStroke(g, 2) {
                g.drawLine(
                    caret.column * measures.letterWidth,
                    caret.line * measures.letterHeight,
                    caret.column * measures.letterWidth,
                    (caret.line + 1) * measures.letterHeight
                )
            }
        }
    }
}