package models

import editor.TextEditorModel
import helpers.Event
import ruleProviders.AggregateFormattingRuleProvider
import ruleProviders.BracketFormattingRuleProvider
import ruleProviders.SelectionFormattingRuleProvider
import ruleProviders.TokenizerFormattingRuleProvider
import tokenizer.Tokenizer

class MainModel (private val codeSource: ICodeSource, defaultText:String) {
    var textModel = TextEditorModel(defaultText)
    val tokenizer = Tokenizer()
    private var tokenizedTextModel = TokenizedTextModel(textModel)
    var formattingRuleProvider = createFormattingRuleProvider()

    val onTextModelChanged = Event<Unit>()

    private fun createFormattingRuleProvider(): AggregateFormattingRuleProvider {
        val r1 = TokenizerFormattingRuleProvider(tokenizedTextModel)
        val r2 = SelectionFormattingRuleProvider(textModel)
        val r3 = BracketFormattingRuleProvider(textModel, tokenizedTextModel)
        return AggregateFormattingRuleProvider(r1, r2, r3)
    }

    fun openFile() {
        val code = codeSource.openCode()
        if (code != null) {
            textModel = TextEditorModel(code)
            tokenizedTextModel = TokenizedTextModel(textModel)
            formattingRuleProvider = createFormattingRuleProvider()
            onTextModelChanged(Unit)
        }
    }

    fun newFile() {
        textModel = TextEditorModel("")
        tokenizedTextModel = TokenizedTextModel(textModel)
        formattingRuleProvider = createFormattingRuleProvider()
        onTextModelChanged(Unit)
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
}