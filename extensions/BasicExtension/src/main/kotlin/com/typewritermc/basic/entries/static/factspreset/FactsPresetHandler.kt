package com.typewritermc.basic.entries.static.factspreset

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.priority
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.interaction.Interaction
import com.typewritermc.engine.paper.entry.entries.Event
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import com.typewritermc.engine.paper.interaction.TriggerContinuation
import com.typewritermc.engine.paper.interaction.TriggerHandler

data class FactsPresetStartTrigger(
    val ref: Ref<FactsPresetEntry>,
    val eventTriggers: List<EventTrigger> = emptyList(),
    val serialization: String = "",
) : EventTrigger {
    override val id: String
        get() = ref.id
    val priority: Int
        get() = ref.priority
}

data object FactsPresetStopTrigger : EventTrigger {
    override val id: String = "basic.factspreset.stop"
}

@Singleton
class FactsPresetHandler : TriggerHandler {
    override suspend fun trigger(
        event: Event,
        currentInteraction: Interaction?
    ): TriggerContinuation {
        if (FactsPresetStopTrigger in event && currentInteraction is FactsPresetInteraction) {
            return TriggerContinuation.Multi(
                TriggerContinuation.EndInteraction,
                TriggerContinuation.Append(
                    Event(
                        event.player,
                        currentInteraction.context,
                        currentInteraction.eventTriggers
                    )
                ),
            )
        }

        return tryStartFactsPresetInteraction(event)
    }

    private fun tryStartFactsPresetInteraction(
        event: Event
    ): TriggerContinuation {
        val triggers = event.triggers.filterIsInstance<FactsPresetStartTrigger>().filter { it.ref.isSet }
        if (triggers.isEmpty()) return TriggerContinuation.Nothing
        val trigger = triggers.maxBy { it.priority }

        return TriggerContinuation.StartInteraction(
            FactsPresetInteraction(
                event.player,
                event.context,
                trigger.ref,
                trigger.eventTriggers,
                trigger.serialization,
            )
        )
    }
}