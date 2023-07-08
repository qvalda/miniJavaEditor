package editor.model

import base.BaseTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TextEditorModelReaderTest: TextEditorModelBaseTest(){

    @Test
    fun testLinesCount() {
        val model = creteTextEditorModelWithCaret("ab|c\r\nde")
        Assertions.assertEquals(2, model.linesCount)
    }

    @Test
    fun testGetLine() {
        val model = creteTextEditorModelWithCaret("ab|c\r\nde")
        Assertions.assertEquals("de", model.getLine(1))
    }

    @Test
    fun testGetLines() {
        val model = creteTextEditorModelWithCaret("ab|c\r\nde")
        BaseTest.assertCollectionEquals(listOf("abc", "de"), model.getLines())
    }
}