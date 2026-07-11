package com.typewritermc.basic.entries.static.factspreset

import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Named
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.extension.annotations.TypewriterCommand
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.command.dsl.*
import com.typewritermc.engine.paper.entry.triggerFor
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

@Singleton
@Named("presetRoots")
fun presetRoots(): Set<Ref<FactsPresetEntry>> {
    val all = mutableSetOf<Ref<FactsPresetEntry>>()
    val children = mutableSetOf<Ref<FactsPresetEntry>>()

    for (preset in Query.find<FactsPresetEntry>()) {
        all.add(preset.ref())
        children.addAll(preset.children)
    }
    return all.subtract(children)
}

@TypewriterCommand
fun CommandTree.factsPreset() = literal("facts") {
    literal("preset") {
        literal("apply") {
            withPermission("typewriter.facts.preset.apply")
            entry<FactsPresetEntry>("preset", filter = {
                val presetRoots =
                    KoinJavaComponent.get<Set<Ref<FactsPresetEntry>>>(Set::class.java, named("presetRoots"))
                it.ref() in presetRoots
            }) { preset ->
                executePlayer { target ->
                    FactsPresetStartTrigger(preset().ref()).triggerFor(target, context())
                }

                string("serialization") { serialization ->
                    executePlayerOrTarget { target ->
                        FactsPresetStartTrigger(preset().ref(), serialization = serialization()).triggerFor(
                            target,
                            context()
                        )
                    }
                }
            }
        }
    }
}
