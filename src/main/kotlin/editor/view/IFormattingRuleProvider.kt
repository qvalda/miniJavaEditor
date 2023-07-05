package editor.view

import helpers.Event

interface IFormattingRuleProvider {
    fun getRules(lineIndex: Int): List<FormattingRule>
    val changed: Event<Unit>
}

abstract class BaseFormattingRuleProvider : IFormattingRuleProvider {
    override val changed = Event<Unit>()
}