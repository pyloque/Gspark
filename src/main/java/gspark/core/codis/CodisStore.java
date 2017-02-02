package gspark.core.codis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gspark.core.config.CodisConfig;
import gspark.core.error.RedisError;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CodisStore {
	private final static Logger LOG = LoggerFactory.getLogger(CodisStore.class);

	private List<JedisPool> pools;

	public CodisStore(CodisConfig config) {
		pools = new ArrayList<JedisPool>(config.getUris().size());
		for (String _uri : config.getUris()) {
			URI uri;
			try {
				uri = new URI(_uri);
			} catch (URISyntaxException e) {
				LOG.error("illegal codis uri {}", _uri, e);
				throw new RedisError("illegal codis uri", e);
			}
			JedisPool pool = new JedisPool(new GenericObjectPoolConfig(), uri, config.getConnectTimeout(),
					config.getSoTimeout());
			pools.add(pool);
		}
	}

	public CodisStore(JedisPool... pools) {
		this.pools = new ArrayList<JedisPool>();
		for (JedisPool pool : pools) {
			this.pools.add(pool);
		}
	}
	
	public List<JedisPool> getPools() {
		return pools;
	}

	public void close() {
		for (JedisPool pool : pools) {
			pool.close();
		}
	}

	public CodisStore execute(Consumer<Jedis> func) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int index = random.nextInt(pools.size());
		JedisPool pool = pools.get(index);
		try (Jedis jedis = pool.getResource()) {
			func.accept(jedis);
		}
		return this;
	}

}
