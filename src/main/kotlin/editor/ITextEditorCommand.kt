package editor

interface ITextEditorCommand {
    fun execute()
    fun undo()
}