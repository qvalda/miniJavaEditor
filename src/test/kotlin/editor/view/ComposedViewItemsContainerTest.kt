package editor.view

import base.BaseTest.Companion.assertCollectionEquals
import mocks.ViewItemMock
import mocks.ViewItemsContainerMock
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.awt.Dimension

class ComposedViewItemsContainerTest {

    @Test
    fun getOnItemsUpdated() {
        val inner1 = ViewItemsContainerMock()
        val inner2 = ViewItemsContainerMock()
        val composed = ComposedViewItemsContainer(inner1, inner2)
        var called = false
        composed.onItemsUpdated += { called = true }
        inner1.invokeOnItemsUpdated()
        assertTrue(called)
    }

    @Test
    fun getSize() {
        val inner1 = ViewItemsContainerMock()
        inner1.size = Dimension(3, 4)
        val inner2 = ViewItemsContainerMock()
        inner2.size = Dimension(5, 6)
        val composed = ComposedViewItemsContainer(inner1, inner2)
        assertEquals(5, composed.size.width)
        assertEquals(6, composed.size.height)
    }

    @Test
    fun getItems() {
        val items1 = mapOf(0 to listOf(ViewItemMock(1), ViewItemMock(2)))
        val inner1 = ViewItemsContainerMock(items1)
        val items2 = mapOf(0 to listOf(ViewItemMock(3)), 1 to listOf(ViewItemMock(4)))
        val inner2 = ViewItemsContainerMock(items2)
        val composed = ComposedViewItemsContainer(inner1, inner2)
        val composedItems = composed.getItems(0)
        assertEquals(3, composedItems.size)
        assertCollectionEquals(listOf(ViewItemMock(1), ViewItemMock(2), ViewItemMock(3)), composedItems)
    }
}