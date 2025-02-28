package me.gabber235.typewriter.db;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import me.gabber235.typewriter.Typewriter;
import me.gabber235.typewriter.facts.FactData;
import me.gabber235.typewriter.facts.FactId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RedisManager extends RedisAbstract {

    private final Gson gson;
    private final RedisProxyMap redisProxyMap;
    private final ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();

    public RedisManager(RedisProxyMap redisProxyMap, RedisClient lettuceRedisClient, int poolSize) {
        super(lettuceRedisClient, poolSize);
        gson = new Gson();
        this.redisProxyMap = redisProxyMap;
        this.registerSub(new String[]{RedisKeys.FACTS_UPDATE.getKey()});
    }

    public void saveUsername(@NotNull UUID uuid, @NotNull String username) {
        getConnectionAsync(c -> c.hset(RedisKeys.USERNAMES.getKey(), username, uuid.toString()));
    }

    public CompletionStage<UUID> loadUsername(String username) {
        return getConnectionAsync(c -> c.hget(RedisKeys.USERNAMES.getKey(), username)
                .thenApply(UUID::fromString));
    }

    public CompletableFuture<Collection<String>> getUsernameThatMatch(String username) {
        final String usernameLowerCase = username.toLowerCase();
        return getConnectionAsync(c -> c.hkeys(RedisKeys.USERNAMES.getKey())
                .thenApply(list -> {
                    if (list.size() > 50) {
                        return list.stream().filter(s -> s.toLowerCase().startsWith(usernameLowerCase)).limit(50).toList();
                    }
                    return list.stream().filter(s -> s.toLowerCase().startsWith(usernameLowerCase)).toList();
                })
                .thenApply(d -> (Collection<String>) d)
        ).toCompletableFuture();
    }

    public CompletionStage<Map<FactId, FactData>> loadFacts() {
        return getConnectionAsync(c -> c.hgetall(RedisKeys.FACS.getKey())
                .thenApply(this::deserializeFactMap));
    }

    public void saveFacts(Map<FactId, FactData> facts) {
        String factDataAsString = serializeFactMap(facts);
        getConnectionAsync(c -> c.hset(RedisKeys.FACS.getKey(), facts.toString(), factDataAsString));
    }

    public void saveFact(FactId factId, FactData factData) {
//        System.out.println("Pre send update: " + factId + " " + factData);
        String factIdAsString = gson.toJson(factId);
        String factDataAsString = serializeFactData(factData);
        getConnectionAsync(c -> c.hset(RedisKeys.FACS.getKey(), factIdAsString, factDataAsString));
        sendUpdate(factId, factData);
    }

    public CompletionStage<FactData> loadFact(FactId factId) {
        String factIdAsString = gson.toJson(factId);
        return getConnectionAsync(c -> c.hget(RedisKeys.FACS.getKey(), factIdAsString)
                .thenApply(s -> s == null ? null : deserializeFactData(s)));
    }

    public void deleteFact(FactId factId) {
        String factIdAsString = gson.toJson(factId);
        getConnectionAsync(c -> c.hdel(RedisKeys.FACS.getKey(), factIdAsString));
        sendUpdate(factId, null);
    }

    public void sendUpdate(FactId factId, @Nullable FactData factData) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("data", serializePairJson(factId, factData));
        jsonObject.addProperty("server", getServerName());
        getConnectionAsync(c -> c.publish(RedisKeys.FACTS_UPDATE.getKey(), jsonObject.toString()));
    }

    public String getServerName() {
        return new File(System.getProperty("user.dir")).getName();
    }

    private Map<FactId, FactData> deserializeFactMap(String json) {
        final JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
        final Map<FactId, FactData> map = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final FactId factId = gson.fromJson(jsonObject.get("key"), FactId.class);
            final FactData factData = deserializeFactData(jsonObject.get("value").getAsJsonObject());
            map.put(factId, factData);
        }
        return map;
    }

    private Map<FactId, FactData> deserializeFactMap(Map<String, String> map) {
        final Map<FactId, FactData> factMap = new HashMap<>();
        map.forEach((key, value) -> {
            final FactId factId = gson.fromJson(key, FactId.class);
            final FactData factData = deserializeFactData(value);
            factMap.put(factId, factData);
        });
        return factMap;
    }

    private FactData deserializeFactData(JsonObject jsonObject) {
        final int value = jsonObject.get("value").getAsInt();
        final long lastUpdated = jsonObject.get("lastUpdated").getAsLong();
        return new FactData(value, LocalDateTime.ofEpochSecond(lastUpdated, 0, zoneOffset));
    }

    private FactData deserializeFactData(String json) {
        return deserializeFactData(gson.fromJson(json, JsonObject.class));
    }

    private String serializeFactData(FactData factData) {
        return serializeFactDataJson(factData).toString();
    }

    private JsonObject serializeFactDataJson(FactData factData) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("value", gson.toJsonTree(factData.getValue()));
        jsonObject.add("lastUpdated", gson.toJsonTree(factData.getLastUpdate().toEpochSecond(zoneOffset)));
        return jsonObject;
    }

    private String serializeFactMap(Map<FactId, FactData> map) {
        final JsonArray jsonArray = new JsonArray();
        map.forEach((key, value) -> {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.add("key", gson.toJsonTree(key));
            jsonObject.addProperty("value", serializeFactData(value));
            jsonArray.add(jsonObject);
        });
        return jsonArray.toString();
    }

    private Pair<FactId, FactData> deserializePair(String json) {
        final JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        final FactId factId = gson.fromJson(jsonObject.get("key"), FactId.class);
        final FactData factData = !jsonObject.has("value") ? null : deserializeFactData(jsonObject.get("value").getAsJsonObject());
        return Pair.of(factId, factData);
    }

    private String serializePair(FactId factId, @Nullable FactData factData) {
        return serializePairJson(factId, factData).toString();
    }

    private JsonObject serializePairJson(FactId factId, @Nullable FactData factData) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("key", gson.toJsonTree(factId));
        if (factData != null) {
            jsonObject.add("value", serializeFactDataJson(factData));

        }
        return jsonObject;
    }

    @Override
    public void receiveMessage(String channel, String message) {
        if (message == null) return;
        if (channel.equals(RedisKeys.FACTS_UPDATE.getKey())) {
            final JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            final String server = jsonObject.get("server").getAsString();
            if (server.equals(getServerName())) return;
            final Pair<FactId, FactData> pair = deserializePair(jsonObject.get("data").toString());
            if (pair.value == null) {
                redisProxyMap.forceRemove(pair.key);
            } else {
                redisProxyMap.forceUpdate(pair.key, pair.value);
            }
        }
    }

    record Pair<K, V>(K key, V value) {

        public static <K, V> Pair<K, V> of(K key, V value) {
            return new Pair<>(key, value);
        }
    }

    private enum RedisKeys {
        FACS("TW_FACTS"),
        FACTS_UPDATE("TW_FACTS_UPDATE"),
        USERNAMES("TW_USERNAMES");

        private final String key;

        RedisKeys(String value) {
            key = value;
        }

        public String getKey() {
            return key;
        }
    }
}
