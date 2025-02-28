package me.gabber235.typewriter.adapters.editors

import com.google.gson.JsonObject
import lirand.api.extensions.server.mainWorld
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.CustomEditor
import me.gabber235.typewriter.adapters.ObjectEditor
import me.gabber235.typewriter.adapters.modifiers.ContentEditor
import me.gabber235.typewriter.adapters.modifiers.ContentEditorModifierComputer
import me.gabber235.typewriter.adapters.modifiers.TargetLocation
import me.gabber235.typewriter.content.ContentContext
import me.gabber235.typewriter.content.modes.ImmediateFieldValueContentMode
import me.gabber235.typewriter.utils.logErrorIfNull
import me.gabber235.typewriter.utils.round
import org.bukkit.Location
import org.bukkit.entity.Player

@CustomEditor(TargetLocation::class)
fun ObjectEditor<TargetLocation>.location() = reference {
    default {
        val obj = JsonObject()
        obj.addProperty("world", "")
        obj.addProperty("x", 0.0)
        obj.addProperty("y", 0.0)
        obj.addProperty("z", 0.0)
        obj.addProperty("yaw", 0.0)
        obj.addProperty("pitch", 0.0)

        obj
    }

    jsonDeserialize { element, _, _ ->
        val obj = element.asJsonObject
        val world = obj.getAsJsonPrimitive("world").asString
        val x = obj.getAsJsonPrimitive("x").asDouble
        val y = obj.getAsJsonPrimitive("y").asDouble
        val z = obj.getAsJsonPrimitive("z").asDouble
        val yaw = obj.getAsJsonPrimitive("yaw")?.asFloat ?: 0f
        val pitch = obj.getAsJsonPrimitive("pitch")?.asFloat ?: 0f


        TargetLocation(world, x, y, z, yaw, pitch)
    }

    jsonSerialize { src, _, _ ->
        val obj = JsonObject()
        obj.addProperty("world", src.world)
        obj.addProperty("x", src.x)
        obj.addProperty("y", src.y)
        obj.addProperty("z", src.z)
        obj.addProperty("yaw", src.yaw)
        obj.addProperty("pitch", src.pitch)

        obj
    }

    ContentEditorModifierComputer with ContentEditor(TargetLocationContentMode::class)
}

class TargetLocationContentMode(context: ContentContext, player: Player) :
    ImmediateFieldValueContentMode<TargetLocation>(context, player) {
    override fun value(): TargetLocation {
        val location = player.location
        val world = location.world
        val x = location.x.round(2)
        val y = location.y.round(2)
        val z = location.z.round(2)
        val yaw = location.yaw.round(2)
        val pitch = location.pitch.round(2)

        return TargetLocation(world.name, x, y, z, yaw, pitch)
    }
}