package editor.model

interface IClipboard {
    fun getData(): String?
    fun setData(text: String)
}