package editor.view.item

import editor.model.TextEditorCaret
import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver
import java.awt.Graphics

class Caret(private val caret: TextEditorCaret): IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        DrawStateSaver.usingColor(g, Style.Caret.color!!) {
            DrawStateSaver.usingStroke(g, 2) {
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