package editor.view.item

import editor.view.DrawMeasures
import mocks.GraphicsMock
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class StringRowTest {

    @Test
    fun drawTest() {
        val view = StringRow("abc")

        val g = GraphicsMock()

        view.draw(g, 2, DrawMeasures(10,2,10))
    }
}