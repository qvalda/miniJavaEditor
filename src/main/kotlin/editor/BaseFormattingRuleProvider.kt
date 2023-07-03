package editor

import helpers.Event

abstract class BaseFormattingRuleProvider : IFormattingRuleProvider {
    override val changed = Event<Unit>()
}