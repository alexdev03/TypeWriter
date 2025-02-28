package me.gabber235.typewriter.entries.cinematic

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Segments
import me.gabber235.typewriter.adapters.modifiers.TargetLocation
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.cinematic.SimpleCinematicAction
import me.gabber235.typewriter.entry.entries.CinematicAction
import me.gabber235.typewriter.entry.entries.CinematicEntry
import me.gabber235.typewriter.entry.entries.Segment
import me.gabber235.typewriter.extensions.packetevents.sendPacketTo
import me.gabber235.typewriter.extensions.packetevents.toVector3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

@Entry("set_fake_block_cinematic", "Set a fake block", Colors.CYAN, "mingcute:cube-3d-fill")
class SetFakeBlockCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(icon = "mingcute:cube-3d-fill")
    @Help("The segments that will be displayed in the cinematic")
    val segments: List<SetFakeBlockSegment> = emptyList(),
) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return SetFakeBlockCinematicAction(player, this)
    }
}

data class SetFakeBlockSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
    val location: TargetLocation = TargetLocation(null, 0.0, 0.0, 0.0),
    val block: Material = Material.AIR,
) : Segment

class SetFakeBlockCinematicAction(
    private val player: Player,
    entry: SetFakeBlockCinematicEntry,
) : SimpleCinematicAction<SetFakeBlockSegment>() {
    override val segments: List<SetFakeBlockSegment> = entry.segments

    override suspend fun startSegment(segment: SetFakeBlockSegment) {
        super.startSegment(segment)

        val state = SpigotConversionUtil.fromBukkitBlockData(segment.block.createBlockData())
        val packet = WrapperPlayServerBlockChange(segment.location.toLocation(player).toVector3i(), state.globalId)
        packet.sendPacketTo(player)
    }

    override suspend fun stopSegment(segment: SetFakeBlockSegment) {
        super.stopSegment(segment)

        player.sendBlockChange(segment.location.toLocation(player), segment.location.toLocation(player).block.blockData)
    }
}

