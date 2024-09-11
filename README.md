# Full-Featured Redis Solution with Redisson: Caching, Message Queue, Pub/Sub, and Distributed Structures

<br>

## Overview
This project demonstrates a complete implementation of <strong>Redisson’s features, including advanced caching mechanisms, distributed collections, message Queue, and pub/sub messaging.</strong>
Ideal for applications requiring <mark>robust Redis functionalities</mark> and <mark>scalable solutions.</mark>
  
## Usages
-  WebFlux 
- Redisson "Redis"

## Architecture of the Project

 ### 1-src folder
   - Configuration
   - Features
     
### 2-Maven pom.xml
<br> 
    
```
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webflux</artifactId>
		</dependency>
		<dependency>
			<groupId>org.redisson</groupId>
			<artifactId>redisson-spring-boot-starter</artifactId>
			<version>3.17.7</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.projectreactor</groupId>
			<artifactId>reactor-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
 ```

<br>

###### Output :star_struck:

##### :pencil2: `Initializes RMapReactive with the name "map" and a StringCodec for serialization. then performs two asynchronous put operations to add entries "map1" and "map2" with respective values "First Map" and "Second Map". Finally, it uses StepVerifier to validate that both put operations complete successfully.` 

###### Code :computer:

<br>

```
	@Test
	public void testMap() {

		RMapReactive<String, String> map = this.client.getMap("map", StringCodec.INSTANCE);
		Mono<String> map1 = map.put("map1", "First Map");
		Mono<String> map2 = map.put("map2", "Secound Map");
		StepVerifier.create(map1.concatWith(map2)).verifyComplete();
	}
```

<br>

###### Result :star_struck:

<br>

<img width="260" alt="map" src="https://github.com/user-attachments/assets/996ceb67-05ea-47df-ae00-f7cc7ececfcb">

---

<br>

##### :pencil2: `Initializes RLocalCachedMap named "cacheMap" is created with a JSON codec TypedJsonJacksonCodec for serialization of Integer keys and String values. cachedMapStrategies defines the caching strategies used. testLocalCacheMap1 will adds two entries key 1 with "cachemap1" and key 2 with "cacheMap2" to the local cached map. testLocalCacheMap2 will update a single entry key1 with "cacheMap1" to the local cached map.` 

###### Code :computer:

<br>

```
  LocalCachedMapOptions<Integer, String> cachedMapStrategies = LocalCachedMapOptions.<Integer, String>defaults()
				.syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE) // you can choose None or Invalidate
				.reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE); //  you can choose Clean

  private RLocalCachedMap<Integer, String> cachedMap = redissonClient.getLocalCachedMap("cacheMap",	new TypedJsonJacksonCodec(Integer.class, String.class),	cachedMapStrategies);

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
```

<br>

###### Result :star_struck: As you can see cache map reflect the updated

<br>

<img width="275" alt="cacheMap" src="https://github.com/user-attachments/assets/1eaaecfd-35f6-423b-8b26-9f3b12944d11">

---

<br>

##### :pencil2: `Initializes RListReactive named "number" is created with a LongCodec for serialization of long values. list of long numbers from 1 to 10 is created and boxed them as list collect so to send it the same time to be same order. The list.addAll(numbersList).then() operation adds all these numbers to the Redis list. The step verifier is used to ensure this operation completes successfully and verifying size.` 

###### Code :computer:

<br>

```
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
```

<br>

###### Result :star_struck:

<br>

<img width="331" alt="list" src="https://github.com/user-attachments/assets/f737a54a-b41e-47f9-b4ad-f41ad7a2b8d4">

---

<br>

##### :pencil2: `Initializes RQueueReactive named "number" is created with a LongCodec for serialization of long values. The method polls items from the queue using queue.poll(), repeating the operation 3 times. Step verifier ensures that the polling operation completes successfully. Additionally, it verifies that the size of the queue is 6 after the polling operations, confirming that the expected number of elements is present.` 

###### Code :computer:

<br>

```
	@Test
	public void testQueue() {

		RQueueReactive<Long> queue = this.client.getQueue("number", LongCodec.INSTANCE);
		Mono<Void> poll = queue.poll().repeat(3).doOnNext(System.out::println).then();
		StepVerifier.create(poll).verifyComplete();
		StepVerifier.create(queue.size())
				.expectNext(6)
				.verifyComplete();
	}
```

<br>

###### Result :star_struck:

<br>

<img width="330" alt="queue" src="https://github.com/user-attachments/assets/a6fbde37-b56b-4627-9e0f-c539d1d0f56e">

---

<br>

##### :pencil2: `Initializes RDequeReactive named "number" is created with a LongCodec for serialization of long values. The method performs a pollLast() operation to remove  items from the end of the stack, repeating this operation 3 times. Step verifier ensures that the polling operation completes successfully. It also verifies that the size of the stack is 2 after the polling operations, confirming the remaining number of elements in the stack.` 

###### Code :computer:

<br>

```
	@Test
	public void testStack() {

		RDequeReactive<Long> stack = this.client.getDeque("number", LongCodec.INSTANCE);
		Mono<Void> poll = stack.pollLast().repeat(3).doOnNext(System.out::println).then();
		StepVerifier.create(poll).verifyComplete();
		StepVerifier.create(stack.size())
				.expectNext(2)
				.verifyComplete();
	}
```

<br>

###### Result :star_struck:

<br>

<img width="295" alt="stack" src="https://github.com/user-attachments/assets/d54aa0ce-7247-4bb2-982f-99e1f6bece45">

---

<br>

##### :pencil2: `Initializes RScoredSortedSetReactive named "name:score" is created with a StringCodec for serialization of string values and scores. The method adds three entries to the sorted set with associated scores: "Sam" with a score of 1.0, "Mike" with a score of 2.5, and "Jake" with a score of 0.5. Step verifier is used to ensure the completion of the score addition. Afterward, the sorted set entries are retrieved in a specified range from rank 0 to 1.` 

###### Code :computer:

<br>

```
@Test
	public void testSortedSet() {

		private RScoredSortedSetReactive<String> sortedSet = this.client.getScoredSortedSet("name:score", StringCodec.INSTANCE);
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
```

<br>

###### Result :star_struck: As you can see Jake is priority than Sam, if you restart will be the score of Jake is priority than Sam.

<br>

<img width="1109" alt="sortedSet-1" src="https://github.com/user-attachments/assets/1e790cff-b60c-4175-a0fe-d30c9081650c">
<img width="1129" alt="sortedSet-2" src="https://github.com/user-attachments/assets/7c9d5abc-6046-470b-a3a3-130cd280689b">

---

<br>

##### :pencil2: `Producer adds messages to the message queue. It generates a sequence of numbers from 1 to 20, with a delay of 500 milliseconds between each numbe. Consumer1 and consumer2 will listents to a message queue to consume messages then print it.` 

###### Code :computer:

<br>

```
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
```

<br>

###### Result :star_struck:

<br>

<img width="1121" alt="producer" src="https://github.com/user-attachments/assets/42678053-b77d-41a3-8138-38ed3d477149">
<img width="1154" alt="consumer-1" src="https://github.com/user-attachments/assets/ff8b61ae-2c29-4f4f-ac53-b68215c47a0e">
<img width="1152" alt="consumer-2" src="https://github.com/user-attachments/assets/fbf01c32-52be-41ff-86a0-69953f239642">


---

<br>

##### :pencil2: `PubSub1 and PubSub2 subscribes to a redis topic to receive messages of type String. It prints each received message to the console.` 

###### Code :computer:

<br>

```
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
```

<br>

###### Result :star_struck:

<br>

<img width="1159" alt="subPub-1" src="https://github.com/user-attachments/assets/723584cd-0038-4d9a-8099-bb7e31635369">
<img width="1137" alt="subPub-2" src="https://github.com/user-attachments/assets/8ed108fe-8c01-49b5-a150-4fd63368fa05">

---

<br>

---

### Good Luck <img src="https://media.giphy.com/media/hvRJCLFzcasrR4ia7z/giphy.gif" width="30px"> 
