package editor

import helpers.Event

interface IFormattingRuleProvider {
    fun getRules(lineIndex: Int): List<FormattingRule>
    val changed: Event<Unit>
}