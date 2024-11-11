package com.typewritermc.engine.paper.db

import com.typewritermc.core.db.RoundRobinConnectionPool
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function

abstract class RedisAbstract(private var lettuceRedisClient: RedisClient, poolSize: Int) {
    private val roundRobinConnectionPool =
        RoundRobinConnectionPool({ lettuceRedisClient.connect() }, poolSize)
    private val pubSubConnections = ConcurrentHashMap<Array<String>, StatefulRedisPubSubConnection<String, String>>()

    abstract fun receiveMessage(channel: String?, message: String?)

    protected fun registerSub(listenedChannels: Array<String>) {
        if (listenedChannels.size == 0) {
            return
        }
        val pubSubConnection = lettuceRedisClient.connectPubSub()
        pubSubConnections[listenedChannels] = pubSubConnection
        pubSubConnection.addListener(object : RedisPubSubListener<String?, String?> {
            override fun message(channel: String?, message: String?) {
                receiveMessage(channel, message)
            }

            override fun message(pattern: String?, channel: String?, message: String?) {
            }

            override fun subscribed(channel: String?, count: Long) {
            }

            override fun psubscribed(pattern: String?, count: Long) {
            }

            override fun unsubscribed(channel: String?, count: Long) {
            }

            override fun punsubscribed(pattern: String?, count: Long) {
            }
        })
        pubSubConnection.async().subscribe(*listenedChannels)
            .exceptionally { throwable: Throwable ->
                throwable.printStackTrace()
                null
            }
    }

    fun <T> getConnectionAsync(redisCallBack: Function<RedisAsyncCommands<String, String>?, CompletionStage<T>?>): CompletionStage<T>? {
        return redisCallBack.apply(roundRobinConnectionPool.get().async())
    }

    fun <T> getConnectionPipeline(redisCallBack: Function<RedisAsyncCommands<String, String>?, CompletionStage<T>?>): CompletionStage<T>? {
        val connection = roundRobinConnectionPool.get()
        connection.setAutoFlushCommands(false)
        val completionStage = redisCallBack.apply(connection.async())
        connection.flushCommands()
        connection.setAutoFlushCommands(true)
        return completionStage
    }

    fun executeTransaction(redisCommandsConsumer: Consumer<RedisCommands<String, String>?>): Optional<MutableList<Any>> {
        val syncCommands = roundRobinConnectionPool.get().sync()
        syncCommands.multi()
        redisCommandsConsumer.accept(syncCommands)
        val transactionResult = syncCommands.exec()
        return Optional.ofNullable(if (transactionResult.wasDiscarded()) null else transactionResult.stream().toList())
    }

    fun close() {
        pubSubConnections.values.forEach(Consumer { obj: StatefulRedisPubSubConnection<String, String> -> obj.close() })
        Bukkit.getLogger().info("Closing pubsub connection")
        lettuceRedisClient.shutdown()
        Bukkit.getLogger().info("Lettuce shutdown connection")
    }
}