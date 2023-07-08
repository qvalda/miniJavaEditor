package mocks

import editor.view.DrawMeasures
import editor.view.IViewItemsContainer
import editor.view.item.IViewItem
import helpers.Event
import java.awt.Dimension
import java.awt.Graphics

class ViewItemsContainerMock(private val items: Map<Int, List<IViewItem>>? = null): IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()
    override var size = Dimension()

    override fun getItems(lineIndex: Int): List<IViewItem> {
        return items?.get(lineIndex) ?: emptyList()
    }

    fun invokeOnItemsUpdated()    {
        onItemsUpdated(Unit)
    }
}

class ViewItemMock(val id: Int): IViewItem{

    override fun draw(g: Graphics, lineIndex: Int, measures: DrawMeasures) {

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViewItemMock

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}