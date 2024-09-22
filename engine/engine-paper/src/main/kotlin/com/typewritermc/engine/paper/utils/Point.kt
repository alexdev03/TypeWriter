package com.typewritermc.engine.paper.utils

import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.util.Vector3i
import com.typewritermc.core.utils.point.Point
import com.typewritermc.core.utils.point.Position
import com.typewritermc.core.utils.point.Vector
import com.typewritermc.core.utils.point.World
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import lirand.api.extensions.server.mainWorld
import lirand.api.extensions.server.server
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

fun org.bukkit.util.Vector.toVector(): Vector {
    return Vector(x, y, z)
}

fun Point.toPacketVector3f() = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
fun Point.toPacketVector3d() = Vector3d(x, y, z)
fun Point.toPacketVector3i() = Vector3i(blockX, blockY, blockZ)
fun Point.toBukkitVector(): org.bukkit.util.Vector = org.bukkit.util.Vector(x, y, z)

fun World.toBukkitWorld(): org.bukkit.World = server.getWorld(UUID.fromString(identifier))
    ?: throw IllegalArgumentException("Could not find world '$identifier' for location, and no default world available.")

fun Position.toBukkitLocation(): org.bukkit.Location = org.bukkit.Location(world.toBukkitWorld(), x, y, z, yaw, pitch)
fun Position.toBukkitPlayerLocation(player: Player): org.bukkit.Location = toLocation(player, this)
fun Position.toPlayerPosition(player: Player): Position = toBukkitPlayerLocation(player).toPosition()
fun org.bukkit.Location.toPlayerPosition(player: Player) : org.bukkit.Location = toPosition().toBukkitPlayerLocation(player)

fun Position.toPacketLocation(): Location = toBukkitLocation().toPacketLocation()

fun org.bukkit.Location.toPosition(): Position = Position(World(world.uid.toString()), x, y, z, yaw, pitch)
fun org.bukkit.Location.toPacketLocation(): Location = SpigotConversionUtil.fromBukkitLocation(this)

fun Location.toCoordinate(): com.typewritermc.core.utils.point.Coordinate =
    com.typewritermc.core.utils.point.Coordinate(x, y, z, yaw, pitch)

val Player.position: Position
    get() = location.toPosition()

fun world(player: Player?, position: Position): World {
    val world = position.world.identifier
    if (player != null && world.isNotEmpty()) {
        val pWorld = PlaceholderAPI.setPlaceholders(player, world)
        val bukkitWorld = server.getWorld(pWorld)
        if (bukkitWorld != null) {
            return World(pWorld)
        }
    }

    return defaultWorld(position)
}

private fun defaultWorld(position: Position): World {
    val world = position.world.identifier
    val bukkitWorld =
        world.let { server.getWorld(it) } ?: server.worlds.firstOrNull { it.name.equals(world, true) }
            .logErrorIfNull(
                "Could not find world '$world' for location, so picking default world. Possible worlds: ${
                    server.worlds.joinToString(
                        ", "
                    ) { "'${it.name}'" }
                }"
            )
        ?: server.mainWorld

    return World(bukkitWorld.name)
}

fun toLocation(player: Player?, position: Position): org.bukkit.Location {
    val worldName = position.world.identifier
    if (player != null && (worldName.isEmpty())) {
        return player.location.clone().apply {
            world = player.world
            x += position.x
            y += position.y
            z += position.z
            yaw += position.yaw
            pitch += position.pitch
        }
    }

    return org.bukkit.Location(Bukkit.getWorld(world(player, position).identifier), position.x, position.y, position.z, position.yaw, position.pitch)
}