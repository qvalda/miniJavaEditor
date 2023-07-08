package editor.view.item

import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver
import java.awt.Graphics

class Selection(private val columnFrom: Int, private val columnTo: Int) : IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        DrawStateSaver.usingColor(g, Style.Selection.background!!) {
            g.fillRect(
                columnFrom * measures.letterWidth,
                lineIndex * measures.letterHeight,
                (columnTo - columnFrom) * measures.letterWidth,
                measures.letterHeight
            )
        }
    }
}