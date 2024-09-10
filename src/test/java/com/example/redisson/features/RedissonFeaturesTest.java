package com.example.redisson.features;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RBlockingDequeReactive;
import org.redisson.api.RDequeReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RMapReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;

import com.example.redisson.features.config.RedissonConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RedissonFeaturesTest extends RedissonFeaturesApplicationTests {

	private RLocalCachedMap<Integer, String> cachedMap;
	private RBlockingDequeReactive<Long> messageQueue;
	private RScoredSortedSetReactive<String> sortedSet;
	private RTopicReactive topic;

	@BeforeAll
	public void setUp() {

		RedissonConfig redissonConfig = new RedissonConfig();
		RedissonClient redissonClient = redissonConfig.redissonClient();
	
		LocalCachedMapOptions<Integer, String> cachedMapStrategies = LocalCachedMapOptions.<Integer, String>defaults()
				.syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE) // None , Invalidate
				.reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE); // Clean

		cachedMap = redissonClient.getLocalCachedMap("cacheMap",
				new TypedJsonJacksonCodec(Integer.class, String.class),
				cachedMapStrategies);

		this.messageQueue = this.client.getBlockingDeque("message-queue", LongCodec.INSTANCE);

		this.topic = this.client.getTopic("room", StringCodec.INSTANCE);

		this.sortedSet = this.client.getScoredSortedSet("name:score", StringCodec.INSTANCE);
	}

	@Test
	public void testMap() {

		RMapReactive<String, String> map = this.client.getMap("map", StringCodec.INSTANCE);
		Mono<String> map1 = map.put("map1", "First Map");
		Mono<String> map2 = map.put("map2", "Secound Map");
		StepVerifier.create(map1.concatWith(map2)).verifyComplete();
	}

	@Test
	public void testLocalCacheMap1() {

		this.cachedMap.put(1, "cachemap1");
		this.cachedMap.put(2, "cacheMap2");
		Flux.interval(Duration.ofSeconds(1))
				.doOnNext(item -> System.out.println(item + "-->" + cachedMap.get(item))).subscribe();
	}

	@Test
	public void testLocalCacheMap2() {

		this.cachedMap.put(1, "cacheMap1");
		Flux.interval(Duration.ofSeconds(1))
				.doOnNext(item -> System.out.println(item + "-->" + cachedMap.get(item))).subscribe();
	}

	@Test
	public void testList() {

		RListReactive<Long> list = this.client.getList("number", LongCodec.INSTANCE);
		List<Long> numbersList = LongStream.rangeClosed(1, 10)
				.boxed().collect(Collectors.toList());
		/* Another Way
		 Mono<Void> numbersList = Flux.range(1, 10)
		 		.flatMap(number -> list.add(number)).then();
		 */

		StepVerifier.create(list.addAll(numbersList).then()).verifyComplete();
		StepVerifier.create(list.size())
				.expectNext(10)
				.verifyComplete();
	}

	@Test
	public void testQueue() {

		RQueueReactive<Long> queue = this.client.getQueue("number", LongCodec.INSTANCE);
		Mono<Void> poll = queue.poll().repeat(3).doOnNext(System.out::println).then();
		StepVerifier.create(poll).verifyComplete();
		StepVerifier.create(queue.size())
				.expectNext(6)
				.verifyComplete();
	}

	@Test
	public void testStack() {

		RDequeReactive<Long> stack = this.client.getDeque("number", LongCodec.INSTANCE);
		Mono<Void> poll = stack.pollLast().repeat(3).doOnNext(System.out::println).then();
		StepVerifier.create(poll).verifyComplete();
		StepVerifier.create(stack.size())
				.expectNext(2)
				.verifyComplete();
	}

	@Test
	public void testMessageQueueConsumer1() throws InterruptedException {

		this.messageQueue.takeElements()
				.doOnNext(elem -> System.out.println("Consumer 1: " + elem))
				.doOnError(System.out::println)
				.subscribe();

				Thread.sleep(600_000);
	}

	@Test
	public void testMessageQueueConsumer2() throws InterruptedException {

		this.messageQueue.takeElements()
				.doOnNext(elem -> System.out.println("Consumer 2: " + elem))
				.doOnError(System.out::println)
				.subscribe();

				Thread.sleep(600_000);
	}

	@Test
	public void testMessageQueueProducer() {

		Mono<Void> numbers = Flux.range(1, 20)
				.delayElements(Duration.ofMillis(500))
				.doOnNext(number -> System.out.println("going to add" + number))
				.flatMap(number -> this.messageQueue.add(Long.valueOf(number)))
				.then();
		StepVerifier.create(numbers).verifyComplete();
	}

	@Test
	public void testPubSub1() throws InterruptedException {
		this.topic.getMessages(String.class)
				.doOnNext(System.out::println)
				.doOnError(System.out::println)
				.subscribe();

				Thread.sleep(600_000);
	}

	@Test
	public void testPubSub2() throws InterruptedException {
		this.topic.getMessages(String.class)
				.doOnNext(System.out::println)
				.doOnError(System.out::println)
				.subscribe();

				Thread.sleep(600_000);
	}

	@Test
	public void testSortedSet() {
	
		Mono<Void> sort = this.sortedSet.addScore("Sam", 1.0).then
		(this.sortedSet.addScore("Mike", 2.5))
		.then(this.sortedSet.addScore("jake", 0.5)).then();
		StepVerifier.create(sort).verifyComplete();
		this.sortedSet.entryRange(0, 1)
				.flatMapIterable(items -> items)
				.map(item -> item.getScore() + ":" + item.getValue())
				.doOnNext(System.out::println)
				.subscribe();

	}

}