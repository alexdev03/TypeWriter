package com.typewritermc.engine.paper.utils.item

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.interaction.InteractionContext
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@AlgebraicTypeInfo("mmo_item", Colors.MEDIUM_SEA_GREEN, "mdi:alpha-m")
class MMOItem(
    private val itemType: String = "",
    private val itemId: String = "",
    ) : Item {

    override fun build(player: Player?, context: InteractionContext?): ItemStack {
        return MMOItems.plugin.getItem(itemType, itemId) ?: ItemStack(Material.STONE)
    }

    override fun isSameAs(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return MMOItems.plugin.getItem(itemType, itemId)?.isSimilar(item) ?: false
    }

    override fun exactMatch(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return MMOItems.plugin.getItem(itemType, itemId)?.equals(item) ?: false
    }
}