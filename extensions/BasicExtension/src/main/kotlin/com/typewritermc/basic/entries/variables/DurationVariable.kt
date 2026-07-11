package com.typewritermc.basic.entries.variables

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.exceptions.ContextDataNotFoundException
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.GenericConstraint
import com.typewritermc.core.extension.annotations.VariableData
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.utils.TICK_MS
import java.time.Duration

@Entry(
    "duration_variable",
    "Converts a numeric value and unit into a Duration",
    Colors.GREEN,
    "material-symbols:timer-rounded"
)
@GenericConstraint(Duration::class)
@VariableData(DurationVariableData::class)
class DurationVariable(
    override val id: String = "",
    override val name: String = "",
) : VariableEntry {
    override fun <T : Any> get(context: VarContext<T>): T {
        val data = context.getData<DurationVariableData>()
            ?: throw ContextDataNotFoundException(context.klass, context.data, id)

        val amount: Int = data.amount.get(context.player, context.interactionContext)
        val duration: Duration = when (data.unit) {
            TemporalUnit.MILLISECONDS -> Duration.ofMillis(amount.toLong())
            TemporalUnit.SECONDS -> Duration.ofSeconds(amount.toLong())
            TemporalUnit.MINUTES -> Duration.ofMinutes(amount.toLong())
            TemporalUnit.HOURS -> Duration.ofHours(amount.toLong())
            TemporalUnit.DAYS -> Duration.ofDays(amount.toLong())
            TemporalUnit.TICKS -> Duration.ofMillis(amount.toLong() * TICK_MS)
        }
        return context.cast(duration)
    }
}

/** Temporal units supported by DurationVariable */
enum class TemporalUnit {
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS,
    TICKS,
}

/** Data for DurationVariable */
data class DurationVariableData(
    val amount: Var<Int> = ConstVar(0),
    @Default("\"SECONDS\"")
    val unit: TemporalUnit = TemporalUnit.SECONDS,
)
