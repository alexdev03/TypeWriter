package com.typewritermc.entity.entries.data.minecraft.living.player

import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entity.SkinProperty
import com.typewritermc.engine.paper.entry.entries.EntityProperty

data class NameAndSkinDataProperty(
    val name: String = "",
    val skin: SkinProperty = SkinProperty(),
) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<NameAndSkinDataProperty>(NameAndSkinDataProperty::class)
}