package com.typewritermc.mythicmobs.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.mythicmobs.entries.data.ListGetVariable
import com.typewritermc.mythicmobs.entries.data.VariableGetElement
import com.typewritermc.mythicmobs.entries.data.VariableSetElement
import io.lumine.mythic.api.mobs.MythicMob
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.entity.Player


@Entry("on_mythic_mob_die", "When a player kill a MythicMobs mob.", Colors.YELLOW, "fa6-solid:skull")
/**
 * The `Mob Death Event` event is triggered when a player kill a mob.
 *
 * ## How could this be used?
 *
 * After killing a final boss, a dialogue or cinematic section can start. The player could also get a special reward the first time they kill a specific mob.
 */
class MythicMobDeathEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Only trigger when a specific mob dies.")
    @Regex
    val mobName: String = "",
    @Placeholder
    val variables: Var<ListGetVariable> = ConstVar(ListGetVariable()),
) : EventEntry

@EntryListener(MythicMobDeathEventEntry::class)
fun onMobDeath(event: MythicMobDeathEvent, query: Query<MythicMobDeathEventEntry>) {
    val player = event.killer as? Player ?: return
    query.findWhere {
        val variables = it.variables.get(player).list
        it.mobName.toRegex(RegexOption.IGNORE_CASE).matches(event.mobType.internalName) && checkIfMobHasAllVariables(
            event.mob,
            variables,
            player
        )
    }.triggerAllFor(player, context())
}

private fun checkIfMobHasAllVariables(mob: ActiveMob, variablesMap: List<VariableGetElement>, player: Player): Boolean {
    if (variablesMap.isEmpty()) return true
    return variablesMap.all { it.has(mob, player) }
}
