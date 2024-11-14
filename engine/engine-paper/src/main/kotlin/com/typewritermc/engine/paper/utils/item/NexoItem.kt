package com.typewritermc.engine.paper.utils.item

import com.nexomc.nexo.api.NexoItems
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.interaction.InteractionContext
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@AlgebraicTypeInfo("nexo_item", Colors.MEDIUM_SEA_GREEN, "mdi:alpha-n")
class NexoItem(
    private val itemId: String = "",
    ) : Item {

    override fun build(player: Player?, context: InteractionContext?): ItemStack {
        return NexoItems.itemFromId(itemId)?.build() ?: ItemStack(Material.STONE)
    }

    override fun isSameAs(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return NexoItems.itemFromId(itemId)?.build()?.isSimilar(item) ?: false
    }

    override fun exactMatch(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return NexoItems.itemFromId(itemId)?.build()?.equals(item) ?: false
    }
}