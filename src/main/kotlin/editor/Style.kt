package editor

import java.awt.Color

class Style(val color: Color? = null, val background: Color? = null, val underline: Color? = null, val isBold : Boolean = false){
    companion object {

        val BackgroundColor = Color(40, 42, 54)
        val CurrentLineColor = Color(68, 71, 90)
        val SelectionColor = Color(68, 71, 90)
        val ForegroundColor = Color(248, 248, 242)
        val CommentColor = Color(98, 114, 164)
        val CyanColor = Color(139, 233, 253)
        val GreenColor = Color(80, 250, 123)
        val OrangeColor = Color(255, 184, 108)
        val PinkColor = Color(255, 121, 198)
        val PurpleColor = Color(189, 147, 249)
        val RedColor = Color(255, 85, 85)
        val YellowColor = Color(241, 250, 140)

        val Background = Style(color = ForegroundColor, background = BackgroundColor)
        val Caret = Style(color = CyanColor)
        val Selection = Style(background = SelectionColor)
        val Comment = Style(color = CommentColor)
        val KeyWord = Style(color = PinkColor)
        val Error = Style(underline = RedColor)
        val Number = Style(color = PurpleColor)
        val String = Style(color = YellowColor)
        val Bracket = Style(background = OrangeColor)
    }
}