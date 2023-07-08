package helpers

import java.awt.*

object DrawStateSaver {
    fun usingColor(g: Graphics, color: Color, statement: () -> Unit) {
        val prevColor = g.color
        g.color = color
        statement()
        g.color = prevColor
    }

    fun usingBold(g: Graphics, isBold: Boolean, statement: () -> Unit) {
        if (g.font.isBold != isBold) {
            val prevFont = g.font
            g.font = Font(prevFont.name, if (isBold) Font.BOLD else Font.PLAIN, prevFont.size)
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