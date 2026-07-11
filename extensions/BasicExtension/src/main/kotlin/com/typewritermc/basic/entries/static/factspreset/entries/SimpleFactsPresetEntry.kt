package com.typewritermc.basic.entries.static.factspreset.entries

import com.typewritermc.basic.entries.static.factspreset.*
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.facts.FactsModifier
import org.bukkit.entity.Player

@Entry(
    "simple_facts_preset",
    "A simple facts preset that just applies the facts with their values",
    Colors.BLUE,
    "mdi:script-text"
)
class SimpleFactsPresetEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<FactsPresetEntry>> = emptyList(),
    override val presets: List<FactPreset> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : FactsPresetEntry {
    override fun applier(
        player: Player,
        modifier: FactsModifier,
        serializer: FactsPresetSerializer
    ): FactsPresetApplier<*> {
        return SimpleFactsPresetApplier(player, this, modifier, serializer)
    }
}

class SimpleFactsPresetApplier(
    player: Player,
    entry: SimpleFactsPresetEntry,
    modifier: FactsModifier,
    serializer: FactsPresetSerializer
) :
    FactsPresetApplier<SimpleFactsPresetEntry>(player, entry, modifier, serializer) {

    override fun init() {
        super.init()
        modifier.apply(player, entry.presets)
        state = FactsPresetApplierState.FINISHED
    }
}