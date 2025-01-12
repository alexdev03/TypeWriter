package com.typewritermc.entity.entries.entity.custom

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entity.*
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.utils.Sound
import com.typewritermc.entity.entries.entity.minecraft.PlayerEntity
import org.bukkit.entity.Player



@Entry("test_npc_instance", "An test instance of a simplified premade npc", Colors.YELLOW, "material-symbols:account-box")
/**
 * The `NpcInstance` class is an entry that represents an instance of a simplified premade npc.
 */
class TestNpcInstance(
    override val id: String = "",
    override val name: String = "",
    override val definition: Ref<NpcDefinition> = emptyRef(),
    override val spawnLocation: Position = Position.ORIGIN,
    @OnlyTags("generic_entity_data", "living_entity_data", "lines", "player_data")
    override val data: List<Ref<EntityData<*>>> = emptyList(),
    override val activity: Ref<out SharedEntityActivityEntry> = emptyRef(),
) : SimpleEntityInstance

class TestNpcEntity(
    player: Player,
    displayName: Var<String>,
    private val skin: Var<SkinProperty>,
    definition: Ref<out EntityDefinitionEntry>,
) : FakeEntity(player) {
    private val namePlate = NamedEntity(player, displayName, PlayerEntity(player, displayName), definition)

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