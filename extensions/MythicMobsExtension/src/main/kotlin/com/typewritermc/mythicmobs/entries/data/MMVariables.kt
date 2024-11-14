package com.typewritermc.mythicmobs.entries.data

import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableType
import org.bukkit.entity.Player

data class VariableSetElement(
    val name : String = "",
    val type : VariableType = VariableType.STRING,
    val value : String = ""
) {
    fun add(mob : ActiveMob, player : Player? = null) {
        val formattedValue = if (player !=null) value.parsePlaceholders(player) else value
        val variable = Variable.ofType(type, formattedValue)
        mob.variables.put(name, variable)
    }
}

data class VariableGetElement(
    val name : String = "",
    val value : String = ""
) {
    fun get(mob : ActiveMob) : String? {
        return mob.variables.get(name)?.toString()
    }

    fun has(mob : ActiveMob, player: Player?) : Boolean {
        return mob.variables.get(name)?.toString()?.equals(value.parsePlaceholders(player)) ?: false
    }
}

data class ListSetVariable(
    val list : List<VariableSetElement> = listOf(),
)

data class ListGetVariable(
    val list : List<VariableGetElement> = listOf(),
)
