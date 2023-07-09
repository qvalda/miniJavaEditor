package helpers

import helpers.DrawStateSaver.usingBold
import helpers.DrawStateSaver.usingColor
import helpers.DrawStateSaver.usingStroke
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.awt.*

class DrawStateSaverTest {
    @Test
    fun testUsingColor() {
        val graphics = mock(Graphics::class.java)
        `when`(graphics.color).thenReturn(Color.RED)
        usingColor(graphics, Color.GREEN) {
            verify(graphics).color = Color.GREEN
        }
        verify(graphics).color = Color.RED
    }

    @Test
    fun testUsingBold() {
        val graphics = mock(Graphics::class.java)
        `when`(graphics.font).thenReturn(Font(null, Font.PLAIN, 10))
        usingBold(graphics, true) {
            verify(graphics).font = Font(null, Font.BOLD, 10)
        }
        verify(graphics).font = Font(null, Font.PLAIN, 10)
    }

    @Test
    fun testUsingBoldIsTheSame() {
        val graphics = mock(Graphics::class.java)
        `when`(graphics.font).thenReturn(Font(null, Font.BOLD, 10))
        usingBold(graphics, true) {
            verify(graphics, times(0)).font = Font(null, Font.BOLD, 10)
        }
        verify(graphics, times(0)).font = Font(null, Font.PLAIN, 10)
    }

    @Test
    fun testUsingStroke() {
        val graphics = mock(Graphics2D::class.java)
        `when`(graphics.stroke).thenReturn(BasicStroke(1f))
        usingStroke(graphics, 2) {
            verify(graphics).stroke = BasicStroke(2f)
        }
        verify(graphics).stroke = BasicStroke(1f)
    }
}