package editor.view.item

import editor.view.DrawMeasures
import java.awt.Graphics

class StringRow(private val text: String): IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        val lineY = measures.letterHeight + lineIndex * measures.letterHeight - measures.letterShift
        g.drawString(text, 0, lineY)
    }
}