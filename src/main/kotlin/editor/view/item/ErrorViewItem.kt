package editor.view.item

import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver
import java.awt.Graphics

class ErrorViewItem(private val start: Int, private val end: Int, private val style: Style) : IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        DrawStateSaver.usingColor(g, style.underline!!) {
            DrawStateSaver.usingStroke(g, 2) {
                g.drawLine(
                    start * measures.letterWidth,
                    lineIndex * measures.letterHeight + measures.letterHeight,
                    end * measures.letterWidth,
                    lineIndex * measures.letterHeight + measures.letterHeight
                )
            }
        }
    }
}