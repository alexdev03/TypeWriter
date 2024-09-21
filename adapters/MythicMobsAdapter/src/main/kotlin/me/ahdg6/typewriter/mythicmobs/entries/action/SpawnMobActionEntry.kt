package me.ahdg6.typewriter.mythicmobs.entries.action

import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableType
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.adapters.modifiers.TargetLocation
import me.gabber235.typewriter.adapters.modifiers.WithRotation
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.TriggerableEntry
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.extensions.placeholderapi.parsePlaceholders
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.ThreadType.SYNC
import org.bukkit.Location
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
    @Help("The mob's name")
    @Placeholder
    private val mobName: String = "",
    @Help("The mob's level")
    private val level: Double = 1.0,
    @Help("Whether the mob should be only seen by the player")
    private val onlyVisibleForPlayer: Boolean = false,
    @Help("The mob's variables in the format of 'variable:type=value'")
    @Placeholder
    private val variables : List<String> = emptyList(),
    @Help("The mob's spawn location")
    @WithRotation
    private var spawnLocation: TargetLocation,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        val mob = MythicBukkit.inst().mobManager.getMythicMob(mobName.parsePlaceholders(player))
        if (!mob.isPresent) return

        println(mob.get())
        println(mobName.parsePlaceholders(player))

        SYNC.launch {
            val activeMob = mob.get().spawn(BukkitAdapter.adapt(spawnLocation.toLocation(player)), level, SpawnReason.OTHER) {
                if (onlyVisibleForPlayer) {
                    it.isVisibleByDefault = false
                    player.showEntity(plugin, it)
                }

            }

            val variableMap = mutableMapOf<String, Any>()
            val variableType = mutableMapOf<String, VariableType>()
            variables.forEach { variable ->
                val split = variable.split("=")
                if (split.size == 2) {
                    val split2 = split[0].split(":")
                    if (split2.size == 2) {
                        println("Type b ${split2[1].uppercase()}")
                        val type = VariableType.valueOf(split2[1].uppercase())
                        println("Type a $type")
                        val name = split2[0]
                        variableMap[name] = split[1].parsePlaceholders(player)
                        variableType[name] = type
                    }
                }
            }
            println("VariableMap $variableMap")
            println("VariableType $variableType")
v            println(activeMob.variables)
            variableMap.forEach { (key, value) ->
                val type = variableType[key]!!
                println("Variable $key $value $type")
                val variable = Variable.ofType(variableType[key]!!, value)
                println("Variable $variable")
                activeMob.variables.put(key, variable)
            }

            println(activeMob.variables)
        }
    }
}