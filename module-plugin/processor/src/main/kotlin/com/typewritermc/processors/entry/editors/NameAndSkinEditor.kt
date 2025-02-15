package com.typewritermc.processors.entry.editors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import com.typewritermc.processors.entry.CustomEditor
import com.typewritermc.processors.entry.DataBlueprint
import com.typewritermc.processors.entry.DataBlueprint.ObjectBlueprint
import com.typewritermc.processors.entry.DataBlueprint.PrimitiveBlueprint
import com.typewritermc.processors.entry.PrimitiveType
import com.typewritermc.processors.whenClassNameIs
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object NameAndSkinEditor : CustomEditor {
    override val id: String = "namdAndSkin"

    override fun accept(type: KSType): Boolean {
        return type whenClassNameIs "com.typewritermc.engine.paper.entry.entity.NameAndSkinDataProperty"
    }

    context(KSPLogger, Resolver) override fun default(type: KSType): JsonElement {
        val skinObject = JsonObject(
            mapOf(
                "texture" to JsonPrimitive(""),
                "signature" to JsonPrimitive(""),
            )
        )
        return JsonObject(
            mapOf(
                "name" to JsonPrimitive(""),
                "skin" to skinObject,
            )
        )
    }

    context(KSPLogger, Resolver) override fun shape(type: KSType): DataBlueprint {
        return ObjectBlueprint(
            mapOf(
                "name" to PrimitiveBlueprint(PrimitiveType.STRING),
                "skin" to ObjectBlueprint(
                    mapOf(
                        "texture" to PrimitiveBlueprint(PrimitiveType.STRING),
                        "signature" to PrimitiveBlueprint(PrimitiveType.STRING),
                    )
                )
            )
        )
    }
}