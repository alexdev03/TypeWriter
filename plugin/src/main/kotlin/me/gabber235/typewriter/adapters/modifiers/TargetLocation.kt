package me.gabber235.typewriter.adapters.modifiers

import lirand.api.extensions.server.mainWorld
import lirand.api.extensions.server.server
import me.clip.placeholderapi.PlaceholderAPI
import me.gabber235.typewriter.utils.logErrorIfNull
import org.bukkit.World
import org.bukkit.entity.Player

data class TargetLocation(
    var world: String? = "",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var yaw: Float = 0f,
    var pitch: Float = 0f
) {
    fun world(player: Player?): World {
        if (player != null && world?.isNotEmpty() == true) {
            val pWorld = PlaceholderAPI.setPlaceholders(player, world!!)
            val bukkitWorld = server.getWorld(pWorld)
            if (bukkitWorld != null) {
                return bukkitWorld
            }
        }

        return defaultWorld()
    }

    private fun defaultWorld(): World {
        val bukkitWorld =
            world?.let { server.getWorld(it) } ?: server.worlds.firstOrNull { it.name.equals(world, true) }
                .logErrorIfNull(
                    "Could not find world '$world' for location, so picking default world. Possible worlds: ${
                        server.worlds.joinToString(
                            ", "
                        ) { "'${it.name}'" }
                    }"
                )
            ?: server.mainWorld

        return bukkitWorld
    }

    fun toLocation(player: Player?): org.bukkit.Location {
        if (player != null && (world == null || world!!.isEmpty())) {
            return player.location.clone().apply {
                world = player.world
                x += this@TargetLocation.x
                y += this@TargetLocation.y
                z += this@TargetLocation.z
                yaw += this@TargetLocation.yaw
                pitch += this@TargetLocation.pitch
            }
        }

        return org.bukkit.Location(world(player), x, y, z, yaw, pitch)
    }


    companion object {
        fun fromLocation(location: org.bukkit.Location): TargetLocation {
            return TargetLocation(location.world.name, location.x, location.y, location.z, location.yaw, location.pitch)
        }

        fun fromPlayer(player: Player): TargetLocation {
            return TargetLocation(
                player.world.name,
                player.location.x,
                player.location.y,
                player.location.z,
                player.location.yaw,
                player.location.pitch
            )
        }

    }
}


