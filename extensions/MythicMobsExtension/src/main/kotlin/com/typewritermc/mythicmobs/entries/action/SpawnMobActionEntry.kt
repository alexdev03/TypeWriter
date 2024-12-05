package com.typewritermc.mythicmobs.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.extension.annotations.WithRotation
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.get
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.ThreadType.SYNC
import com.typewritermc.engine.paper.utils.toBukkitPlayerLocation
import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableType
import org.bukkit.entity.Player


@Entry("spawn_mythicmobs_mob", "Spawn a mob from MythicMobs", Colors.ORANGE, "fa6-solid:dragon")
/**
 * The `Spawn Mob Action` action spawn MythicMobs mobs to the world.
 *
 * ## How could this be used?
 *
 * This action could be used in a plethora of scenarios. From simple quests requiring you to kill some spawned mobs, to complex storylines that simulate entire battles, this action knows no bounds!
 */
class SpawnMobActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Placeholder
    private val mobName: Var<String> = ConstVar(""),
    private val level: Var<Double> = ConstVar(1.0),
    private val onlyVisibleForPlayer: Boolean = false,
    private val persist: Boolean = true,
    @Placeholder
    @Help("The variables to set for the mob. Format: variableName:type=value. Example: test:INTEGER=100")
    private val variables : List<String> = emptyList(),
    @WithRotation
    private var spawnLocation: Var<Position> = ConstVar(Position.ORIGIN),
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        val mob = MythicBukkit.inst().mobManager.getMythicMob(mobName.get(player).parsePlaceholders(player))
        if (!mob.isPresent) return

        SYNC.launch {
            val activeMob = mob.get().spawn(BukkitAdapter.adapt(spawnLocation.get(player).toBukkitPlayerLocation(player)), level.get(player), SpawnReason.OTHER) {
                if (onlyVisibleForPlayer) {
                    it.isVisibleByDefault = false
                    player.showEntity(plugin, it)
                }

                it.isPersistent = persist
            }

            val variableMap = mutableMapOf<String, Any>()
            val variableType = mutableMapOf<String, VariableType>()
            variables.forEach { variable ->
                val split = variable.split("=")
                if (split.size == 2) {
                    val split2 = split[0].split(":")
                    if (split2.size == 2) {
                        val type = VariableType.valueOf(split2[1].uppercase())
                        val name = split2[0]
                        variableMap[name] = split[1].parsePlaceholders(player)
                        variableType[name] = type
                    }
                }
            }

            variableMap.forEach { (key, value) ->
                val variable = Variable.ofType(variableType[key]!!, value)
                activeMob.variables.put(key, variable)
            }

        }

    }
}