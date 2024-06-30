package com.nextdevv.automod.redis.redisdata;

import io.lettuce.core.api.StatefulRedisConnection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RoundRobinConnectionPool<K,V> {
    private final AtomicInteger next = new AtomicInteger(0);
    private final StatefulRedisConnection[] elements;
    private final Supplier<StatefulRedisConnection<K,V>> statefulRedisConnectionSupplier;

    public RoundRobinConnectionPool(Supplier<StatefulRedisConnection<K,V>> statefulRedisConnectionSupplier, int poolSize) {
        this.statefulRedisConnectionSupplier = statefulRedisConnectionSupplier;
        this.elements = new StatefulRedisConnection[poolSize];
        for(int i = 0; i < poolSize; i++) {
            elements[i] = statefulRedisConnectionSupplier.get();
        }
    }

    public StatefulRedisConnection<K, V> get() {
        int index = next.getAndIncrement() % elements.length;
        StatefulRedisConnection<K, V> connection = elements[index];
        if (connection != null)
            if (connection.isOpen())
                return connection;

        connection = statefulRedisConnectionSupplier.get();
        elements[index] = connection;
        return connection;
    }

}
