package com.typewritermc.mythicmobs.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.Sync
import com.typewritermc.mythicmobs.entries.data.ListGetVariable
import io.lumine.mythic.bukkit.MythicBukkit
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit


@Entry("despawn_mythicmobs_mobs", "Despawn MythicMobs mobs from variable", Colors.ORANGE_RED, "fluent:crown-subtract-24-filled")

class DespawnMobsActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Placeholder
    val variables: Var<ListGetVariable> = ConstVar(ListGetVariable()),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val variables = variables.get(player).list
        val mobs = MythicBukkit.inst().mobManager.activeMobs.filter { variables.all { v -> v.has(it, player) } }

        if (Bukkit.isPrimaryThread()) {
            mobs.forEach {
                it.remove()
            }
            return
        }

        Dispatchers.Sync.launch {
            mobs.forEach {
                it.remove()
            }
        }
    }
}