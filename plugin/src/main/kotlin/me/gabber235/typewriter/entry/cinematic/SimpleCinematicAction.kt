package me.gabber235.typewriter.entry.cinematic

import me.gabber235.typewriter.entry.entries.CinematicAction
import me.gabber235.typewriter.entry.entries.Segment
import me.gabber235.typewriter.entry.entries.activeSegmentAt
import me.gabber235.typewriter.entry.entries.canFinishAt
import org.bukkit.entity.Player

abstract class SimpleCinematicAction<S : Segment> : CinematicAction {
    protected var lastFrame = 0
        private set
    private var previousSegment: S? = null

    override suspend fun tick(frame: Int, player: Player) {
        lastFrame = frame
        super.tick(frame, player)
        val segment = segments activeSegmentAt frame

        if (segment == previousSegment) {
            segment?.let { tickSegment(it, frame, player) }
            return
        }

        previousSegment?.let { stopSegment(it, player) }

        segment?.let {
            startSegment(it, player)
            tickSegment(it, frame, player)
        }
    }

    override suspend fun teardown(player: Player) {
        super.teardown(player)
        previousSegment?.let { stopSegment(it, player) }
        previousSegment = null
    }

    override fun canFinish(frame: Int): Boolean = segments canFinishAt frame

    protected open suspend fun startSegment(segment: S, player: Player) {
        previousSegment = segment
    }

    protected open suspend fun stopSegment(segment: S, player: Player) {
        previousSegment = null
    }

    protected open suspend fun tickSegment(segment: S, frame: Int, player: Player) {
    }

    abstract val segments: List<S>
}