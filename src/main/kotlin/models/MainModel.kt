package models

import editor.model.TextEditorModel
import helpers.Event
import helpers.ThrottleCall
import ruleProviders.*

class MainModel (private val codeSource: ICodeSource, defaultText:String) {
    val onTextModelChanged = Event<Unit>()

    lateinit var textModel : TextEditorModel
    private lateinit var tokenizedTextModel : TokenizedTextModel
    private lateinit var parsedTextModel : ParsedTextModel
    lateinit var formattingRuleProvider : AggregateFormattingRuleProvider

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

    private fun createFormattingRuleProvider(): AggregateFormattingRuleProvider {
        val r1 = TokenizerFormattingRuleProvider(tokenizedTextModel)
        val r2 = SelectionFormattingRuleProvider(textModel)
        val r3 = BracketFormattingRuleProvider(textModel, tokenizedTextModel)
        val r4 = ParserFormattingRuleProvider(parsedTextModel, tokenizedTextModel)
        val r5 = UniqueClassNameVisitor(parsedTextModel, tokenizedTextModel)
        return AggregateFormattingRuleProvider(r1, r2, r3, r4, r5)//r3, r4, r5)
    }

    private fun createModels(input:String){
        if(this::tokenizedTextModel.isInitialized){
            tokenizedTextModel.modified -= ::onTokenizedTextModelModifiedDelayed
        }

        textModel = TextEditorModel(input)
        tokenizedTextModel = TokenizedTextModel(textModel)
        parsedTextModel = ParsedTextModel(tokenizedTextModel.asTokenSource())
        formattingRuleProvider = createFormattingRuleProvider()
        onTextModelChanged(Unit)

        tokenizedTextModel.modified += ::onTokenizedTextModelModifiedDelayed
    }

    val onTokenizedTextModelThrottled = ThrottleCall(500) { onTokenizedTextModelModified() }

    private fun onTokenizedTextModelModifiedDelayed(unit:Unit) {
        onTokenizedTextModelThrottled()
    }

    private fun onTokenizedTextModelModified() {
        parsedTextModel.update(tokenizedTextModel.asTokenSource())
    }
}