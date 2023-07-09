package editor.view.item

import editor.view.DrawMeasures
import editor.view.Style
import helpers.DrawStateSaver.usingBold
import helpers.DrawStateSaver.usingColor
import java.awt.Graphics

class ColoredString(private val text: String, private val column: Int, private val style: Style) : IViewItem {
    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {
        val lineY = measures.letterHeight + lineIndex * measures.letterHeight - measures.letterShift
        usingColor(g, style.color!!) {
            usingBold(g, style.isBold)
            {
                g.drawString(text, column * measures.letterWidth, lineY)
            }
        }
    }
}