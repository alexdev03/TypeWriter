package com.typewritermc.entity.entries.entity.custom

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entity.EntityState
import com.typewritermc.engine.paper.entry.entity.FakeEntity
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entity.SkinProperty
import com.typewritermc.engine.paper.entry.entries.EntityDefinitionEntry
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.entity.entries.entity.minecraft.PlayerEntity
import org.bukkit.entity.Player

class AmberNpcEntity(
    player: Player,
    displayName: Var<String>,
    private val skin: Var<SkinProperty>,
    definition: Ref<out EntityDefinitionEntry>,
) : FakeEntity(player) {
    private val namePlate = AmberNamedEntity(player, displayName, PlayerEntity(player, displayName), definition)

    init {
        consumeProperties(skin.get(player))
    }

    override val entityId: Int
        get() = namePlate.entityId

    override val state: EntityState
        get() = namePlate.state

    override fun applyProperties(properties: List<EntityProperty>) {
        if (properties.any { it is SkinProperty }) {
            namePlate.consumeProperties(properties)
            return
        }
        namePlate.consumeProperties(properties + skin.get(player))
    }

    override fun tick() {
        namePlate.tick()
    }

    override fun spawn(location: PositionProperty) {
        namePlate.spawn(location)
    }

    override fun addPassenger(entity: FakeEntity) {
        namePlate.addPassenger(entity)
    }

    override fun removePassenger(entity: FakeEntity) {
        namePlate.removePassenger(entity)
    }

    override fun contains(entityId: Int): Boolean {
        return namePlate.contains(entityId)
    }

    override fun dispose() {
        namePlate.dispose()
    }
}