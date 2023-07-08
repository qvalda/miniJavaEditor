package helpers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventTest {

    @Test
    fun canAssign() {
        val event = Event<Unit>()
        var called = false
        event += { called = true }

        event(Unit)

        assertTrue(called)
    }

    @Test
    fun canUnassign() {
        val event = Event<Unit>()
        var called = false
        val method: (Unit) -> Unit = { called = true }
        event += method
        event -= method

        event(Unit)

        assertFalse(called)
    }

    @Test
    fun canAssignMultiple() {
        val event = Event<Unit>()
        var called = 0
        event += { called++ }
        event += { called++ }

        event(Unit)

        assertEquals(2, called)
    }
}