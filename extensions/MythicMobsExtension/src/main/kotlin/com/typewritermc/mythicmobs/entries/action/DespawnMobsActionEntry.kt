package com.typewritermc.mythicmobs.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.StagingManager
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.get
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.ThreadType.SYNC
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.placeholders.PlaceholderExecutor.parsePlaceholders
import org.bukkit.entity.Player


@Entry(
    "despawn_mythicmobs_mobs",
    "Despawn MythicMobs mobs from variable",
    Colors.ORANGE_RED,
    "fluent:crown-subtract-24-filled"
)

class DespawnMobsActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The variables to set for the mob. Format: variableName=value. Example: test=100")
    @Placeholder
    private val variables: List<String> = emptyList(),
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)
        //map variables in a map, the key is the variable name, the value is the value parsed with papi
        val variablesMap = variables.associate { it.split("=")[0] to it.split("=")[1].parsePlaceholders(player) }

        val mobs = MythicBukkit.inst().mobManager.activeMobs.filter {
            variablesMap.entries.all { entry ->
                it.variables.has(entry.key)
                        && entry.value == it.variables.get(entry.key).toString()
            }
        }

        SYNC.launch {
            mobs.forEach {
                it.remove()
            }
        }
    }
}