package ackern.core.redis;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CodisStore {

	private List<JedisPool> pools;

	public CodisStore(URI... uris) {
		pools = new ArrayList<JedisPool>(uris.length);
		for (URI uri : uris) {
			JedisPool pool = new JedisPool(uri);
			pools.add(pool);
		}
	}

	public void close() {
		for (JedisPool pool : pools) {
			pool.close();
		}
	}

	public void execute(Consumer<Jedis> func) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int index = random.nextInt(pools.size());
		JedisPool pool = pools.get(index);
		try (Jedis jedis = pool.getResource()) {
			func.accept(jedis);
		}
	}

}
