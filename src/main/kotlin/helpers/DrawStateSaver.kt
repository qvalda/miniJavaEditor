package helpers

import java.awt.*

class DrawStateSaver {
    companion object {

        fun usingColor(g: Graphics, color: Color, statement: () -> Unit) {
            val prevColor = g.color
            g.color = color
            statement()
            g.color = prevColor
        }

        fun usingBold(g: Graphics, isBold: Boolean, statement: () -> Unit) {
            if (isBold) {
                val prevFont = g.font
                g.font = Font(prevFont.name, Font.BOLD, prevFont.size)
                statement()
                g.font = prevFont
            } else {
                statement()
            }
        }

        fun usingStroke(g: Graphics, stroke: Int, statement: () -> Unit) {
            if (g is Graphics2D) {
                val prevStroke = g.stroke
                g.stroke = BasicStroke(stroke.toFloat())
                statement()
                g.stroke = prevStroke
            } else {
                statement()
            }
        }
    }
}