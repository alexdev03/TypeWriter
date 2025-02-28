package me.gabber235.typewriter.entries.event

import lirand.api.extensions.math.blockLocation
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Min
import me.gabber235.typewriter.adapters.modifiers.TargetLocation
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entries.EmptyTrigger
import me.gabber235.typewriter.entry.entries.EventEntry
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

@Entry("on_player_near_location", "When the player is near a certain location", Colors.YELLOW, "mdi:map-marker-radius")
/**
 * The `PlayerNearLocationEventEntry` class represents an event that is triggered when a player is within a certain range of a location.
 *
 * ## How could this be used?
 *
 * This could be used to create immersive gameplay experiences such as triggering a special event or dialogue when a player approaches a specific location.
 * For example, when a player gets close to a hidden treasure, a hint could be revealed or a guardian could spawn.
 */
class PlayerNearLocationEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The location the player should be near.")
    val location: TargetLocation = TargetLocation(null, 0.0, 0.0, 0.0),
    @Help("The range within which the event should trigger.")
    @Min(1)
    val range: Double = 1.0
) : EventEntry

@EntryListener(PlayerNearLocationEventEntry::class)
fun onPlayerNearLocation(event: PlayerMoveEvent, query: Query<PlayerNearLocationEventEntry>) {
    // Only check if the player moved a block
    if (!event.hasChangedBlock()) return

    query findWhere { entry ->
        !event.from.blockLocation.isInRange(
            entry.location.toLocation(event.player),
            entry.range
        ) && event.to.blockLocation.isInRange(entry.location.toLocation(event.player), entry.range)
    } triggerAllFor event.player
}

@EntryListener(PlayerNearLocationEventEntry::class)
fun onPlayerTeleportNearLocation(event: PlayerTeleportEvent, query: Query<PlayerNearLocationEventEntry>) {
    query findWhere { entry ->
        val loc = entry.location.toLocation(event.player)
        !event.from.blockLocation.isInRange(
            loc,
            entry.range
        ) && event.to.blockLocation.isInRange(loc, entry.range)
    } triggerAllFor event.player
}

fun Location.isInRange(location: Location, range: Double): Boolean {
    if (location.world != world) return false
    return this.distanceSquared(location) <= range * range
}