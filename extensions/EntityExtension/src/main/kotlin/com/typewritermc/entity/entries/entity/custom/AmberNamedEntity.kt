package com.typewritermc.entity.entries.entity.custom

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.point.Vector
import com.typewritermc.engine.paper.entry.entity.EntityState
import com.typewritermc.engine.paper.entry.entity.FakeEntity
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.EntityDefinitionEntry
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.LinesProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.Color
import com.typewritermc.engine.paper.utils.asMini
import com.typewritermc.engine.paper.utils.asMiniWithResolvers
import com.typewritermc.engine.paper.utils.isFloodgate
import com.typewritermc.entity.entries.data.minecraft.display.BillboardConstraintProperty
import com.typewritermc.entity.entries.data.minecraft.display.TranslationProperty
import com.typewritermc.entity.entries.data.minecraft.display.text.BackgroundColorProperty
import com.typewritermc.entity.entries.entity.minecraft.TextDisplayEntity
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player


class AmberNamedEntity(
    player: Player,
    private val displayName: Var<String>,
    private val baseEntity: FakeEntity,
    definition: Ref<out EntityDefinitionEntry>,
) : FakeEntity(player) {
    private val hologram = TextDisplayEntity(player)

    override val entityId: Int
        get() = baseEntity.entityId

    override val state: EntityState
        get() = baseEntity.state

    init {
        val hologramText = hologram()
        hologram.consumeProperties(
            LinesProperty(hologramText),
            TranslationProperty(Vector(y = namePlateOffset)),
            BillboardConstraintProperty(AbstractDisplayMeta.BillboardConstraints.CENTER),
            BackgroundColorProperty(Color.fromHex(namePlateColor))
        )
    }

    override fun applyProperties(properties: List<EntityProperty>) {
        return baseEntity.consumeProperties(properties)
    }

    override fun tick() {
        baseEntity.tick()
        val hologramText = hologram()
        hologram.consumeProperties(LinesProperty(hologramText))
        hologram.tick()
    }

    private fun hologram(): String {
        val other = property(LinesProperty::class)?.lines ?: ""
        val displayName = this.displayName

        return namePlate.parsePlaceholders(player).asMiniWithResolvers(
            Placeholder.parsed("other", other),
            Placeholder.parsed("display_name", displayName.get(player).parsePlaceholders(player)),
        ).asMini().trim()
    }

    private fun calculateIndicatorOffset(hologramText: String): Vector {
        val lines = hologramText.count { it == '\n' } + 1
        val height = lines * 0.3 + namePlateOffset
        return Vector(y = height)
    }

    override fun spawn(location: PositionProperty) {
        baseEntity.spawn(location)
        hologram.spawn(location)

        // Since bedrock players don't have TextDisplay entities,
        // we cannot show both the nameplate and the indicator at the same time.
        // So we will only show the nameplate if the player is not using a floodgate.
        if (player.isFloodgate) {
            baseEntity.addPassenger(hologram)
        } else {
            baseEntity.addPassenger(hologram)
        }
    }

    override fun addPassenger(entity: FakeEntity) {
        baseEntity.addPassenger(entity)
    }

    override fun removePassenger(entity: FakeEntity) {
        baseEntity.removePassenger(entity)
    }

    override fun contains(entityId: Int): Boolean {
        if (baseEntity.contains(entityId)) return true
        if (hologram.contains(entityId)) return true
        return false
    }

    override fun dispose() {
        baseEntity.dispose()
        hologram.dispose()
    }
}
