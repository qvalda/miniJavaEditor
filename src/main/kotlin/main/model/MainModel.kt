package main.model

import editor.model.TextEditorModel
import editor.view.ComposedViewItemsContainer
import editor.view.IViewItemsContainer
import editor.view.TextViewItemsContainer
import helpers.Event
import helpers.ThrottleCall
import main.itemsContainer.HighlightedBracketsViewItemsContainer
import main.itemsContainer.ParserViewItemsContainer
import main.itemsContainer.TokenizerViewItemsContainer
import main.itemsContainer.UniqueClassCheckerViewItemsContainer
import parser.ProgramParser
import parser.SignificantTokenSource
import tokenizer.Tokenizer

class MainModel(private val codeSource: ICodeSource, defaultText: String) {

    val onTextModelChanged = Event<Unit>()

    lateinit var textModel: TextEditorModel
    lateinit var visualItemsContainer: IViewItemsContainer

    private lateinit var tokenizedModel: TokenizedModel
    private lateinit var parsedModel: ParsedModel

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
        val t = TokenizerViewItemsContainer(textModel, tokenizedModel)
        val p = ParserViewItemsContainer(tokenizedModel, parsedModel)
        val u = UniqueClassCheckerViewItemsContainer(tokenizedModel, parsedModel)
        val b = HighlightedBracketsViewItemsContainer(textModel, tokenizedModel)
        return ComposedViewItemsContainer(m, t, p, u, b)
    }

    private fun createModels(input: String) {
        if (this::tokenizedModel.isInitialized) {
            tokenizedModel.modified -= ::onTokenizedTextModelModifiedDelayed
        }

        textModel = TextEditorModel(input)
        tokenizedModel = TokenizedModel(textModel, Tokenizer())
        parsedModel = ParsedModel(ProgramParser())
        parsedModel.rebuild(SignificantTokenSource(tokenizedModel.asTokenSource()))
        visualItemsContainer = createVisualItemsContainer()
        tokenizedModel.modified += ::onTokenizedTextModelModifiedDelayed

        onTextModelChanged(Unit)
    }

    val onTokenizedTextModelThrottled = ThrottleCall(500) { onTokenizedTextModelModified() }

    private fun onTokenizedTextModelModifiedDelayed(unit: Unit) {
        onTokenizedTextModelThrottled()
    }

    private fun onTokenizedTextModelModified() {
        parsedModel.rebuild(SignificantTokenSource(tokenizedModel.asTokenSource()))
    }
}