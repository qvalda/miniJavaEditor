import java.awt.Color

class Style(val color: Color? = null, val background: Color? = null, val underline: Color? = null){
    companion object {
        val KeyWord = Style(color = Color(255,240,0))
        val Selection = Style(background = Color(62,126,255))
        val Error = Style(underline = Color(255,70,70))
        val Comment = Style(color = Color(91,172,24))
        val Caret = Style(color = Color(190,190,190))
    }
}

open class FormattingRule(val start: Int, val end:Int, val style: Style)