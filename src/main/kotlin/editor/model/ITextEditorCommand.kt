package editor.model

interface ITextEditorCommand {
    fun execute()
    fun undo()
}