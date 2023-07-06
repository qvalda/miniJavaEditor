package editor.view.item

import editor.view.DrawMeasures
import java.awt.Graphics

interface IViewItem{
    fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures){}
}