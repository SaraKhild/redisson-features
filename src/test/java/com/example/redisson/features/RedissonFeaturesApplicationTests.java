package com.example.redisson.features;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.redisson.api.RedissonReactiveClient;

import com.example.redisson.features.config.RedissonConfig;

/*
 JUnit creates a new instance of the test class for each test method, but with PER_CLASS, 
 JUnit uses the same test instance for all test methods in a test class.
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class RedissonFeaturesApplicationTests {

	private final RedissonConfig redissonConfig = new RedissonConfig();
	protected RedissonReactiveClient client;

	@BeforeAll
	public void setClient() {
		
		this.client = this.redissonConfig.redissonReactiveClient();
	}

	@AfterAll
	public void shutdown() {

		this.client.shutdown();
	}

}
