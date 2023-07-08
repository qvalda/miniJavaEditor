package editor.view

import editor.view.item.IViewItem
import helpers.Event
import java.awt.Dimension

class ComposedViewItemsContainer(private vararg val containers: IViewItemsContainer): IViewItemsContainer {

    override val onItemsUpdated = Event<Unit>()

    init {
        for (c in containers){
            c.onItemsUpdated+= :: onContainerViewModified
        }
    }

    override val size: Dimension
        get() = containers.map { c->c.size }.maxBy { s->s.height }

    override fun getItems(lineIndex: Int): List<IViewItem> {
        return containers.flatMap { c->c.getItems(lineIndex) }
    }

    private fun onContainerViewModified(unit: Unit) {
        onItemsUpdated(unit)
    }
}