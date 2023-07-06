package editor.view

import editor.view.item.IViewItem
import helpers.Event
import java.awt.Dimension

interface IViewItemsContainer {
    val onItemsUpdated : Event<Unit>
    val size: Dimension
        get() = Dimension()

    fun getItems(lineIndex: Int): List<IViewItem>
}