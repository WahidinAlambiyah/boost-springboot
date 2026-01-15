package com.example.graphqlusers.redis;

import redis.clients.jedis.JedisPool;

public final class RedisFactory {
    private RedisFactory() {
    }

    public static JedisPool create(String host, int port) {
        return new JedisPool(host, port);
    }
}
