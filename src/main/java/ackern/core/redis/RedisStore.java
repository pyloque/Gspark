package ackern.core.redis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ackern.core.config.RedisConfig;
import ackern.core.error.RedisError;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisStore {
	private final static Logger LOG = LoggerFactory.getLogger(RedisStore.class);

	private JedisPool pool;

	public RedisStore(RedisConfig config) {
		URI uri = null;
		try {
			uri = new URI(config.getUri());
		} catch (URISyntaxException e) {
			LOG.error("illegal redis uri {}", config.getUri(), e);
			throw new RedisError("illegal redis uri", e);
		}
		this.pool = new JedisPool(new GenericObjectPoolConfig(), uri, config.getConnectTimeout(),
				config.getSoTimeout());
	}

	public void close() {
		pool.close();
	}

	public RedisStore execute(Consumer<Jedis> func) {
		try (Jedis jedis = pool.getResource()) {
			func.accept(jedis);
		}
		return this;
	}

}
