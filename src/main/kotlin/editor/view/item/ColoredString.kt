package editor.view.item

import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver
import java.awt.Graphics

class ColoredString(private val text: String, private val column: Int, private val style: Style): IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        val lineY = measures.letterHeight + lineIndex * measures.letterHeight - measures.letterShift
        DrawStateSaver.usingColor(g, style.color!!) {
            DrawStateSaver.usingBold(g, style.isBold)
            {
                g.drawString(text, column * measures.letterWidth, lineY)
            }
        }
    }
}