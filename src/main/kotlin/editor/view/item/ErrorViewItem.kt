package editor.view.item

import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver
import java.awt.Graphics

class ErrorViewItem(private val message: String, private val start: Int, private val end: Int) : IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        DrawStateSaver.usingColor(g, Style.Error.underline!!) {
            DrawStateSaver.usingStroke(g, 2) {
                g.drawLine(
                    start * measures.letterWidth,
                    (lineIndex + 1) * measures.letterHeight,
                    end * measures.letterWidth,
                    (lineIndex + 1) * measures.letterHeight
                )
            }
        }
    }

    override fun drawTooltip(g: Graphics, lineIndex: Int, columnIndex: Int, measures: DrawMeasures) {

        if (columnIndex < start || columnIndex > end) return

        val x = (columnIndex + 2) * measures.letterWidth
        val y = (lineIndex + 1) * measures.letterHeight

        DrawStateSaver.usingColor(g, Style.ErrorTooltip.background!!) {
            g.fillRect(x, y, message.length * measures.letterWidth, measures.letterHeight)
        }
        DrawStateSaver.usingColor(g, Style.ErrorTooltip.color!!) {
            g.drawString(message, x, y + measures.letterHeight - measures.letterShift)
        }
    }
}