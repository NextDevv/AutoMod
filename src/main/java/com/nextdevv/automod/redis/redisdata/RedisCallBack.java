package com.nextdevv.automod.redis.redisdata;

import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public interface RedisCallBack {
    @FunctionalInterface
    interface PubSub {
        void useConnection(StatefulRedisPubSubConnection<String, String> connection);
    }
}
