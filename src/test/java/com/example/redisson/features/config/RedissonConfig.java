package com.example.redisson.features.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private RedissonClient redissonClient;

    public RedissonClient redissonClient() {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redissonClient = Redisson.create(config);

        return redissonClient;
    }

    public RedissonReactiveClient redissonReactiveClient() {

        return redissonClient().reactive();
    }

}
