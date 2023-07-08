package tokenizer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharArraySafeReaderTest {

    @Test
    fun canReadChars() {
        val reader = createCharArraySafeReader("ab")
        assertEquals('a', reader.currentChar)
        reader.moveNext()
        assertEquals('b', reader.currentChar)
    }

    @Test
    fun returnEOFChars() {
        val reader = createCharArraySafeReader("ab")
        reader.moveNext()
        reader.moveNext()
        assertFalse(reader.moveNext())
        assertTrue(reader.isEOF())
    }

    @Test
    fun doNotThrowAfterAllReads() {
        val reader = createCharArraySafeReader("ab")
        reader.moveNext()
        reader.moveNext()
        reader.moveNext()
        assertEquals(Char.MIN_VALUE, reader.currentChar)
        assertEquals(Char.MIN_VALUE, reader.nextChar)
    }

    private fun createCharArraySafeReader(input: String): CharArraySafeReader {
        return CharArraySafeReader(input)
    }
}