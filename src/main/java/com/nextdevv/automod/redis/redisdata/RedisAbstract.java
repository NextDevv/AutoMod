package com.nextdevv.automod.redis.redisdata;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.AllArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Function;

@AllArgsConstructor
public abstract class RedisAbstract {

    protected static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final RoundRobinConnectionPool<String, String> roundRobinConnectionPoolString;
    protected RedisClient lettuceRedisClient;

    public RedisAbstract(RedisClient lettuceRedisClient, int size) {
        this.lettuceRedisClient = lettuceRedisClient;
        new RoundRobinConnectionPool<>(() -> lettuceRedisClient.connect(new SerializedObjectCodec()), size);
        this.roundRobinConnectionPoolString = new RoundRobinConnectionPool<>(lettuceRedisClient::connect, size);
    }

    public <T> void getConnectionAsync(Function<RedisAsyncCommands<String, String>, CompletionStage<T>> redisCallBack) {
        redisCallBack.apply(roundRobinConnectionPoolString.get().async());
    }

    public boolean isConnected() {
        try (StatefulRedisConnection<String, String> connection = lettuceRedisClient.connect()) {
            return connection.isOpen();
        } catch (Exception e) {
            return false;
        }
    }

    public void getPubSubConnection(RedisCallBack.PubSub redisCallBack) {
        redisCallBack.useConnection(lettuceRedisClient.connectPubSub());
    }

    public void close() {
        lettuceRedisClient.shutdownAsync().thenAccept(v -> executorService.shutdown());
    }
}
