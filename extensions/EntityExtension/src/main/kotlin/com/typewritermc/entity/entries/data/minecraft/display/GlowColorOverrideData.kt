package com.typewritermc.entity.entries.data.minecraft.display

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.extensions.packetevents.metas
import com.typewritermc.engine.paper.utils.Color
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("glow_color_override_data", "Override the glow color of the display entity.", Colors.RED, "material-symbols:move-selection-up-rounded")
@Tags("glow_color_override_data")
class GlowColorOverrideData(
    override val id: String = "",
    override val name: String = "",
    override val priorityOverride: Optional<Int> = Optional.empty(),
    private val color : Var<Color> = ConstVar(Color.BLACK_BACKGROUND),
) : DisplayEntityData<GlowColorOverrideProperty> {
    override fun type(): KClass<GlowColorOverrideProperty> = GlowColorOverrideProperty::class

    override fun build(player: Player): GlowColorOverrideProperty = GlowColorOverrideProperty(color.get(player).toBukkitColor().asRGB())
}

data class GlowColorOverrideProperty(val hex: Int) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<GlowColorOverrideProperty>(
        GlowColorOverrideProperty::class,
        GlowColorOverrideProperty(0xFFFFFF)
    )
}

fun applyGlowColorOverrideData(entity: WrapperEntity, property: GlowColorOverrideProperty) {
    entity.metas {
        meta<AbstractDisplayMeta> { glowColorOverride = property.hex }
        error("Could not apply GlowColorOverrideProperty to ${entity.entityType} entity.")
    }
}