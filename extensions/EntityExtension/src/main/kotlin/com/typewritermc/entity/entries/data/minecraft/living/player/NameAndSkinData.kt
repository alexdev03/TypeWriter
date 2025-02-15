package com.typewritermc.entity.entries.data.minecraft.living.player;

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.NameAndSkinDataProperty
import com.typewritermc.engine.paper.entry.entity.SkinProperty
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityData
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("name_and_skin_data", "Name and skin data for players", Colors.RED, "ant-design:skin-filled")
@Tags("name_and_skin_data", "player_data")
class NameAndSkinDataEntry(
    override val id: String = "",
    override val name: String = "",
    val data: Var<NameAndSkinData> = ConstVar(NameAndSkinData("", SkinProperty())),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<NameAndSkinDataProperty> {

    override fun type(): KClass<NameAndSkinDataProperty> = NameAndSkinDataProperty::class

    override fun build(player: Player): NameAndSkinDataProperty = data.get(player).toProperty()

}

data class NameAndSkinData(
    val name: String = "",
    val skin: SkinProperty = SkinProperty(),
) {
    fun toProperty(): NameAndSkinDataProperty = NameAndSkinDataProperty(name, skin)
}



//gabber dovrebbe fare che è possibile assegnare le proprietà all'esterno di     override fun applyProperties(properties: List<EntityProperty>) {
// le entry in casi speciali dovrebbero avere uno stato
