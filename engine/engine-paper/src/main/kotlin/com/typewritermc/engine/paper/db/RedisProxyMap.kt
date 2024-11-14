package com.typewritermc.engine.paper.db

import com.typewritermc.core.db.RedisManager
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.engine.paper.facts.FactDatabase
import com.typewritermc.engine.paper.facts.FactId
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.ui.CommunicationHandler
import io.lettuce.core.RedisClient
import lirand.api.extensions.events.listen
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.java.KoinJavaComponent

class RedisProxyMap(
    factDatabase: FactDatabase,
    private val map : MutableMap<FactId, FactData>
) : MutableMap<FactId, FactData> by map
{

    val redis: RedisManager

    init {
        val communicationHandler: CommunicationHandler = KoinJavaComponent.get(CommunicationHandler::class.java)
        redis = RedisManager(factDatabase,this, RedisClient.create(communicationHandler.getRedisURI()), 10)
        redis.loadGroupFacts().thenAccept {
            println("Loaded group facts: ${it.size}")
            putAll(it)
        }
//        redis.loadFacts().thenAccept {
//            println("loaded: $it")
//            putAll(it)
//        }.exceptionally { it.printStackTrace(); null }

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