package com.typewritermc.engine.paper.utils.item

import com.nexomc.nexo.api.NexoItems
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.utils.item.components.ItemComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@AlgebraicTypeInfo("nexo_item", Colors.MEDIUM_SEA_GREEN, "mdi:alpha-n")
class NexoItem(
    val itemId: String = "",
    val components: List<ItemComponent> = emptyList(),
    ) : Item {

    inline fun <reified C : ItemComponent> components(): List<C> = components.filterIsInstance<C>()
    inline fun <reified C : ItemComponent> firstComponent(): C? = components.firstOrNull { it is C } as C?

    override fun build(player: Player?, context: InteractionContext?): ItemStack {
        return (NexoItems.itemFromId(itemId)?.build() ?: ItemStack(Material.STONE)).apply {
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