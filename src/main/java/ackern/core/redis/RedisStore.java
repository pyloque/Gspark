package ackern.core.redis;

import java.net.URI;
import java.util.function.Consumer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisStore {

	private JedisPool pool;

	public RedisStore(URI uri) {
		this.pool = new JedisPool(uri);
	}

	public void close() {
		pool.close();
	}

	public void execute(Consumer<Jedis> func) {
		try (Jedis jedis = pool.getResource()) {
			func.accept(jedis);
		}
	}

}
