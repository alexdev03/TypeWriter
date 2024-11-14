package com.typewritermc.engine.paper.facts

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.typewritermc.core.db.RedisManager
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.engine.paper.db.RedisProxyMap
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.ModifierOperator
import com.typewritermc.engine.paper.entry.RefreshFactTrigger
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.ThreadType.DISPATCHERS_ASYNC
import com.typewritermc.engine.paper.utils.logErrorIfNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lirand.api.extensions.events.listen
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

private const val FACT_STORAGE_DELAY = 60 * 3

class FactDatabase : KoinComponent, Listener {
    private val storage: FactStorage by inject()
//    private lateinit var redis: RedisManager

    // Local stored version of player facts
    private val cache = RedisProxyMap(this, ConcurrentHashMap<FactId, FactData>())
    val playerFacts: MutableMap<UUID, MutableSet<FactId>> = Maps.newConcurrentMap()
    val playerUUIDs: MutableSet<UUID> = Sets.newConcurrentHashSet()


    fun initialize() {
        storage.init()
        cache.redis.loadPlayerUUIDs().thenAccept {
            playerUUIDs.addAll(it)
        }
//        redis = RedisManager(this, RedisClient.create(communicationHandler.getRedisURI()), 10)

        // Load all the facts from the storage
//        runBlocking {
////            loadFactsFromPersistentStorage()
//            println("loading facts from redis")
//
//        }

        // Filter expired facts every second.
        // After that, save the facts of the players who have facts that expired or changed.
        DISPATCHERS_ASYNC.launch {
//            var cycle = 1
            while (plugin.isEnabled) {
                delay(1000)
                removeExpiredFacts()

//                if (cycle++ % FACT_STORAGE_DELAY == 0) {
//                    storeFactsInPersistentStorage()
//                }
            }
        }
        plugin.listen<PlayerLoginEvent> { it ->
            if(it.result != PlayerLoginEvent.Result.ALLOWED) {
                return@listen
            }
            if(!playerUUIDs.contains(it.player.uniqueId)) {
                cache.redis.savePlayerUUID(it.player.uniqueId)
                playerUUIDs.add(it.player.uniqueId)
            }
            val uuid = it.player.uniqueId
            val name = it.player.name
            cache.redis.loadPlayerFacts(it.player.uniqueId).thenAccept {
                playerFacts[uuid] = it.keys.toMutableSet()
                it.forEach { (id, data) ->
                    cache.forceUpdate(id, data)
                }
                println("loaded facts for $name")
            }.exceptionally { it.printStackTrace(); null }
        }

        plugin.listen<PlayerQuitEvent> {
            plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable {
                playerFacts.remove(it.player.uniqueId)?.forEach { factId ->
                    cache.forceRemove(factId)
                }
            }, 3)
        }

    }

    fun getRedis() : RedisManager {
        return cache.redis
    }


    fun shutdown() {
        runBlocking {
//            storeFactsInPersistentStorage()
        }
        storage.shutdown()
    }

    private suspend fun loadFactsFromPersistentStorage() {
        val facts = storage.loadFacts()
        cache.clear()
        cache.putAll(facts)
    }

    private suspend fun storeFactsInPersistentStorage() {
        val entries =
            cache.keys.mapNotNull { Query.findById<PersistableFactEntry>(it.entryId) }.associateBy { it.id }

        // If there are no entries, we don't need to store anything.
        if (entries.isEmpty()) return

        val facts = cache.entries.filter { (id, data) ->
            val entry = entries[id.entryId] ?: return@filter false

            // If the fact is not persistable, or it has expired, don't store it.
            if (!entry.canPersist(id, data)) return@filter false
            if (entry is ExpirableFactEntry && entry.hasExpired(id, data)) return@filter false

            true
        }.map { (id, data) -> id to data }

//        redis.saveFacts(facts.stream().collect(Collectors.toMap({ it.first }, { it.second })))
    }

    private fun removeExpiredFacts() {
        val expiredIds = cache.entries.mapNotNull { (id, data) ->
            val entry = Query.findById<ExpirableFactEntry>(id.entryId) ?: return@mapNotNull null
            if (!entry.hasExpired(id, data)) return@mapNotNull null
            id
        }

        expiredIds.forEach { cache.remove(it) }
    }

    internal operator fun get(id: FactId): FactData? = cache[id]

    internal operator fun set(id: FactId, data: FactData) {
        if (data.value == 0) {
            cache.remove(id)
            checkRemovePlayerFact(id)
        } else {
            cache[id] = data
            checkUpdatePlayerFact(id)
        }
    }

    private fun checkUpdatePlayerFact(id: FactId) {
        val uuid = readUUID(id) ?: return
        if(playerUUIDs.contains(uuid)) {
            if (playerFacts[uuid]?.contains(id) == false) {
                playerFacts[uuid]?.add(id)
            }
        }
    }

    private fun checkRemovePlayerFact(id: FactId) {
        val uuid = readUUID(id) ?: return
        if(playerUUIDs.contains(uuid)) {
            playerFacts[uuid]?.remove(id)
        }
    }

    fun readUUID(id: FactId): UUID? {
        return try {
            UUID.fromString(id.groupId.id)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun createFactId(entryId: String, uuid: UUID): FactId {
        return FactId(entryId, GroupId(uuid))
    }

    fun modify(player: Player, modifiers: List<Modifier>) {
        modify(player) {
            modifiers.forEach { modifier ->
                this[modifier.fact] = when (modifier.operator) {
                    ModifierOperator.ADD -> {
                        val entry =
                            modifier.fact.get().logErrorIfNull("Could not find ${modifier.fact}") ?: return@forEach

                        if (entry !is ReadableFactEntry) {
                            plugin.logger.warning("Tried to add to a non-readable fact: ${modifier.fact}, how do you expect to add if you can't read?")
                            return@forEach
                        }

                        val fact = entry.readForPlayersGroup(player)
                        modifier.value.get(player) + fact.value
                    }
                    ModifierOperator.MULTIPLY -> {
                        val entry =
                            modifier.fact.get().logErrorIfNull("Could not find ${modifier.fact}") ?: return@forEach

                        if (entry !is ReadableFactEntry) {
                            plugin.logger.warning("Tried to multiply a non-readable fact: ${modifier.fact}, how do you expect to multiply if you can't read?")
                            return@forEach
                        }

                        val fact = entry.readForPlayersGroup(player)
                        modifier.value.get(player) * fact.value
                    }

                    ModifierOperator.SET -> modifier.value.get(player)
                }
            }
        }
    }

    fun modify(player: Player, modifier: FactsModifier.() -> Unit) {
        val modifications = FactsModifier().apply(modifier).build()
        if (modifications.isEmpty()) return

        for ((id, value) in modifications) {
            val entry = Query.findById<WritableFactEntry>(id) ?: continue
            entry.write(player, value)
            if (entry is ReadableFactEntry) {
                RefreshFactTrigger(entry.ref()) triggerFor player
            }
        }
    }

//    fun updateFact(factId: FactId, factData: FactData) {
//        cache[factId] = factData
//    }
//
//    fun removeFact(key: FactId) {
//        cache.remove(key)
//    }
}

class FactsModifier {
    private val modifications = mutableMapOf<String, Int>()

    operator fun set(ref: Ref<out WritableFactEntry>, value: Int) = set(ref.id, value)

    operator fun set(id: String, value: Int) {
        modifications[id] = value
    }

    fun build(): Map<String, Int> = modifications
}

fun Player.fact(ref: Ref<out ReadableFactEntry>) = ref.get()?.readForPlayersGroup(this)