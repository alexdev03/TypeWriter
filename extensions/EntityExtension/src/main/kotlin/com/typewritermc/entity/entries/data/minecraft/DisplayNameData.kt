package com.typewritermc.entity.entries.data.minecraft

import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.Var

data class DisplayNameProperty(val displayName: Var<String>) : EntityProperty {
    constructor(displayName: String) : this(ConstVar(displayName))
    companion object : SinglePropertyCollectorSupplier<DisplayNameProperty>(DisplayNameProperty::class)
}
