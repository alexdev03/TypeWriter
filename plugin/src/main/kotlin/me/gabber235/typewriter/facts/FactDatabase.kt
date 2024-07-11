package me.gabber235.typewriter.facts

import io.lettuce.core.RedisClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lirand.api.extensions.events.listen
import me.gabber235.typewriter.db.RedisManager
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entries.ExpirableFactEntry
import me.gabber235.typewriter.entry.entries.PersistableFactEntry
import me.gabber235.typewriter.entry.entries.ReadableFactEntry
import me.gabber235.typewriter.entry.entries.WritableFactEntry
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.ui.CommunicationHandler
import me.gabber235.typewriter.utils.ThreadType.DISPATCHERS_ASYNC
import me.gabber235.typewriter.utils.logErrorIfNull
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

private const val FACT_STORAGE_DELAY = 60 * 3

class FactDatabase : KoinComponent {
    private val storage: FactStorage by inject()
    private lateinit var redis: RedisManager

    // Local stored version of player facts
    private val cache = ConcurrentHashMap<FactId, FactData>()

    fun initialize() {
        storage.init()

        val communicationHandler: CommunicationHandler = KoinJavaComponent.get(CommunicationHandler::class.java)
        redis = RedisManager(this, RedisClient.create(communicationHandler.getRedisURI()), 10)

        // Load all the facts from the storage
        runBlocking {
//            loadFactsFromPersistentStorage()
            println("loading facts from redis")
            redis.loadFacts().thenAccept {
                println("loaded: $cache")
                cache.putAll(it)
            }.exceptionally { it.printStackTrace(); null }
        }

        // Filter expired facts every second.
        // After that, save the facts of the players who have facts that expired or changed.
        DISPATCHERS_ASYNC.launch {
            var cycle = 1
            while (plugin.isEnabled) {
                delay(1000)
                removeExpiredFacts()

                if (cycle++ % FACT_STORAGE_DELAY == 0) {
                    storeFactsInPersistentStorage()
                }
            }
        }

        plugin.listen<PlayerJoinEvent> {
            redis.saveUsername(it.player.uniqueId, it.player.name)
        }
    }

    fun getRedis() : RedisManager{
        return redis
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
            redis.deleteFact(id)
        } else {
            cache[id] = data
            redis.saveFact(id, data)
        }
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
                        modifier.value + fact.value
                    }

                    ModifierOperator.SET -> modifier.value
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

    fun updateFact(factId: FactId, factData: FactData) {
        cache[factId] = factData
    }

    fun removeFact(key: FactId) {
        cache.remove(key)
    }
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