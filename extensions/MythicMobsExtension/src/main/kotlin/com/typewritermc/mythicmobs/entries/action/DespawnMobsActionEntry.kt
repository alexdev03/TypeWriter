package com.typewritermc.mythicmobs.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.StagingManager
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.ThreadType.SYNC
import com.typewritermc.mythicmobs.entries.data.ListGetVariable
import com.typewritermc.mythicmobs.entries.data.VariableGetElement
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.placeholders.PlaceholderExecutor.parsePlaceholders
import org.bukkit.Bukkit
import org.bukkit.entity.Player


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

        SYNC.launch {
            mobs.forEach {
                it.remove()
            }
        }
    }
}