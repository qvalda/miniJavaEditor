package models

import editor.view.ComposedViewItemsContainer
import editor.view.IViewItemsContainer
import editor.model.TextEditorModel
import editor.view.TextViewItemsContainer
import helpers.Event
import helpers.ThrottleCall
import parser.SignificantTokenSource

class MainModel (private val codeSource: ICodeSource, defaultText:String) {

    val onTextModelChanged = Event<Unit>()

    lateinit var textModel: TextEditorModel
    lateinit var visualItemsContainer: IViewItemsContainer

    private lateinit var tokenizedTextModel: TokenizedTextModel
    private lateinit var parsedTextModel: ParsedTextModel

    init {
        createModels(defaultText)
    }

    fun openFile() {
        val code = codeSource.openCode()
        if (code != null) {
            createModels(code)
        }
    }

    fun newFile() {
        createModels("")
    }

    fun saveFile() {
        val text = textModel.getText()
        codeSource.saveCode(text)
    }

    fun cutAction() {
        textModel.cutAction()
    }

    fun copyAction() {
        textModel.copyAction()
    }

    fun pasteAction() {
        textModel.pasteAction()
    }

    fun undoAction() {
        textModel.undo()
    }

    fun redoAction() {
        textModel.redo()
    }

    private fun createVisualItemsContainer(): IViewItemsContainer {
        val m = TextViewItemsContainer(textModel)
        val t = TokenizerViewItemsContainer(textModel, tokenizedTextModel)
        val p = ParserViewItemsContainer(parsedTextModel, tokenizedTextModel)
        val u = UniqueClassCheckerViewItemsContainer(parsedTextModel, tokenizedTextModel)
        val b = HighlightedBracketsViewItemsContainer(textModel, tokenizedTextModel)
        return ComposedViewItemsContainer(m, t, p, u, b)
    }

    private fun createModels(input: String) {
        if (this::tokenizedTextModel.isInitialized) {
            tokenizedTextModel.modified -= ::onTokenizedTextModelModifiedDelayed
        }

        textModel = TextEditorModel(input)
        tokenizedTextModel = TokenizedTextModel(textModel)
        parsedTextModel = ParsedTextModel(SignificantTokenSource(tokenizedTextModel.asTokenSource()))
        visualItemsContainer = createVisualItemsContainer()
        tokenizedTextModel.modified += ::onTokenizedTextModelModifiedDelayed

        onTextModelChanged(Unit)
    }

    val onTokenizedTextModelThrottled = ThrottleCall(500) { onTokenizedTextModelModified() }

    private fun onTokenizedTextModelModifiedDelayed(unit: Unit) {
        onTokenizedTextModelThrottled()
    }

    private fun onTokenizedTextModelModified() {
        parsedTextModel.update(SignificantTokenSource(tokenizedTextModel.asTokenSource()))
    }
}