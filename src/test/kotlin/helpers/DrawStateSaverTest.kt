package helpers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.awt.*

class DrawStateSaverTest {
    @Test
    fun usingColor() {
        val graphics = mock(Graphics::class.java)
        `when`(graphics.color).thenReturn(Color.RED)
        DrawStateSaver.usingColor(graphics, Color.GREEN) {
            verify(graphics).color = Color.GREEN
        }
        verify(graphics).color = Color.RED
    }

    @Test
    fun usingBold() {
        val graphics = mock(Graphics::class.java)
        `when`(graphics.font).thenReturn(Font(null, Font.PLAIN, 10))
        DrawStateSaver.usingBold(graphics, true) {
            verify(graphics).font = Font(null, Font.BOLD, 10)
        }
        verify(graphics).font = Font(null, Font.PLAIN, 10)
    }

    @Test
    fun usingBoldIsTheSame() {
        val graphics = mock(Graphics::class.java)
        `when`(graphics.font).thenReturn(Font(null, Font.BOLD, 10))
        DrawStateSaver.usingBold(graphics, true) {
            verify(graphics, times(0)).font = Font(null, Font.BOLD, 10)
        }
        verify(graphics, times(0)).font = Font(null, Font.PLAIN, 10)
    }

    @Test
    fun usingStroke() {
        val graphics = mock(Graphics2D::class.java)
        `when`(graphics.stroke).thenReturn(BasicStroke(1f))
        DrawStateSaver.usingStroke(graphics, 2) {
            verify(graphics).stroke = BasicStroke(2f)
        }
        verify(graphics).stroke = BasicStroke(1f)
    }
}