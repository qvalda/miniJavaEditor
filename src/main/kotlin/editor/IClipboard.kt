package editor

interface IClipboard {
    fun getData(): String?
    fun setData(text: String)
}