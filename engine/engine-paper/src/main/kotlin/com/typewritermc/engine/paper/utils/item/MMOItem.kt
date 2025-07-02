package com.typewritermc.engine.paper.utils.item

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.utils.item.components.ItemComponent
import de.tr7zw.nbtapi.NBT
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

const val ID = "MMOITEMS_ITEM_ID"
const val TYPE = "MMOITEMS_ITEM_TYPE"

@AlgebraicTypeInfo("mmo_item", Colors.MEDIUM_SEA_GREEN, "mdi:alpha-m")
class MMOItem(
    val itemType: String = "",
    val itemId: String = "",
    val components: List<ItemComponent> = emptyList(),
    ) : Item {

    inline fun <reified C : ItemComponent> components(): List<C> = components.filterIsInstance<C>()
    inline fun <reified C : ItemComponent> firstComponent(): C? = components.firstOrNull { it is C } as C?

    override fun build(player: Player?, context: InteractionContext?): ItemStack {
        return (MMOItems.plugin.getItem(itemType.uppercase(), itemId) ?: ItemStack(Material.STONE)).apply {
            components.forEach {
                it.apply(player, context, this)
            }
        }
    }

    override fun isSameAs(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        val itemStack : ItemStack = item ?: return false
        val isCorrect = NBT.get<Boolean>(itemStack) { nbt ->
            return@get nbt.hasTag(ID) && nbt.getString(ID).equals(itemId, true) && nbt.hasTag(TYPE) && nbt.getString(TYPE).equals(itemType, true)
        }

        return isCorrect
    }

    override fun exactMatch(player: Player?, item: ItemStack?, context: InteractionContext?): Boolean {
        return build(player, context) == item
    }
}