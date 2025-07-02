package com.typewritermc.engine.paper.snippets

import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.get
import com.typewritermc.engine.paper.utils.reloadable
import org.bukkit.configuration.file.YamlConfiguration
import org.koin.core.component.KoinComponent
import java.util.logging.Level
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

interface SnippetDatabase {
    fun get(path: String, default: Any, comment: String = ""): Any
    fun <T : Any> getSnippet(path: String, klass: KClass<T>, default: T, comment: String = ""): T
    fun registerSnippet(path: String, defaultValue: Any, comment: String = "")
}

class SnippetDatabaseImpl : SnippetDatabase, KoinComponent {
    private val file by lazy {
        val file = plugin.dataFolder["snippets.yml"]
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        file
    }

    private val ymlConfiguration by reloadable { YamlConfiguration.loadConfiguration(file) }
    private val cache by reloadable { mutableMapOf<String, Any>() }

    override fun get(path: String, default: Any, comment: String): Any {
        val cached = cache[path]
        if (cached != null) return cached

        val value = ymlConfiguration.get(path)

        if (value == null) {
            ymlConfiguration.set(path, default)
            if (comment.isNotBlank()) {
                ymlConfiguration.setComments(path, comment.lines())
            }
            try {
                ymlConfiguration.save(file)
                println("Saved $path")
            } catch (e: Exception) {
                plugin.logger.log(Level.SEVERE, "Could not save config file for $path", e)
            }
            return default
        }

        cache[path] = value
        return value
    }

    override fun <T : Any> getSnippet(path: String, klass: KClass<T>, default: T, comment: String): T {
        val value = get(path, default, comment)

        // First, try a direct cast. This will work for non-numeric types and
        // when the numeric type is already correct.
        val casted = klass.safeCast(value)
        if (casted != null) {
            return casted
        }

        // If the direct cast fails, check for number conversion.
        // This handles cases like reading a Double from YAML when a Float is needed.
        if (value is Number) {
            val converted: Any? = when (klass) {
                Float::class -> value.toFloat()
                Double::class -> value.toDouble()
                Int::class -> value.toInt()
                Long::class -> value.toLong()
                Short::class -> value.toShort()
                Byte::class -> value.toByte()
                else -> null // Not a numeric type we can handle here
            }

            @Suppress("UNCHECKED_CAST")
            if (converted != null && klass.isInstance(converted)) {
                cache[path] = converted
                return converted as T
            }
        }

        // If all casting and conversion fails, the type is genuinely wrong.
        // Reset it to the default, save, and log the issue.
        plugin.logger.warning("Type mismatch for snippet '$path'. Expected ${klass.simpleName} but found ${value::class.simpleName}. Resetting to default value.")
        ymlConfiguration.set(path, default)
        if (comment.isNotBlank()) {
            ymlConfiguration.setComments(path, comment.lines())
        }
        try {
            ymlConfiguration.save(file)
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not save config file for $path", e)
        }

        cache[path] = default
        return default
    }

    override fun registerSnippet(path: String, defaultValue: Any, comment: String) {
        get(path, defaultValue, comment)
    }
}