package com.typewritermc.engine.paper.utils.item

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.utils.item.components.ItemComponent
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@AlgebraicTypeInfo("mmo_item", Colors.MEDIUM_SEA_GREEN, "mdi:alpha-m")
class MMOItem(
    private val itemType: String = "",
    private val itemId: String = "",
    private val components: List<ItemComponent> = emptyList(),
    ) : Item {

    override fun build(player: Player?, context: InteractionContext?): ItemStack {
        return (MMOItems.plugin.getItem(itemType, itemId) ?: ItemStack(Material.STONE)).apply {
            components.forEach {
                it.apply(player, context, this)
            }
        }
    }

    override fun isSameAs(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return build(player, context).isSimilar(item)
    }

    override fun exactMatch(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return build(player, context) == item
    }
}