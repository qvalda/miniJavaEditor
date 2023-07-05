package editor.view

import editor.model.IClipboard
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

class SystemClipboard : IClipboard {
    override fun getData(): String? {
        return Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String?
    }

    override fun setData(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}