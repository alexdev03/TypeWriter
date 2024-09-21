package me.gabber235.typewriter.db

import io.lettuce.core.RedisClient
import lirand.api.extensions.events.listen
import me.gabber235.typewriter.facts.FactData
import me.gabber235.typewriter.facts.FactId
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.ui.CommunicationHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.java.KoinJavaComponent

class RedisProxyMap(
    private val map : MutableMap<FactId, FactData>
) : MutableMap<FactId, FactData> by map
    {

        val redis: RedisManager

        init {
            val communicationHandler: CommunicationHandler = KoinJavaComponent.get(CommunicationHandler::class.java)
            redis = RedisManager(this, RedisClient.create(communicationHandler.getRedisURI()), 10)
            redis.loadFacts().thenAccept {
                println("loaded: $it")
                putAll(it)
            }.exceptionally { it.printStackTrace(); null }

            plugin.listen<PlayerJoinEvent> {
                redis.saveUsername(it.player.uniqueId, it.player.name)
            }
        }

        override fun put(key: FactId, value: FactData): FactData? {
            redis.saveFact(key, value)
            return map.put(key, value)
        }

        override fun remove(key: FactId): FactData? {
            redis.deleteFact(key)
            return map.remove(key)
        }

        fun forceRemove(key: FactId): FactData? {
            return map.remove(key)
        }

        fun forceUpdate(key: FactId, value: FactData) {
            map[key] = value
        }
    }