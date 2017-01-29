package gspark.core.rediscluster;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gspark.core.config.RedisClusterConfig;
import gspark.core.error.RedisError;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisClusterStore {
	private final static Logger LOG = LoggerFactory.getLogger(RedisClusterStore.class);

	private JedisCluster cluster;

	public RedisClusterStore(RedisClusterConfig config) {
		Set<HostAndPort> nodes = new HashSet<HostAndPort>();
		for (String _uri : config.getUris()) {
			URI uri;
			try {
				uri = new URI(_uri);
			} catch (URISyntaxException e) {
				LOG.error("illegal redis cluster uri {}", _uri, e);
				throw new RedisError("illegal redis cluster uri", e);
			}
			HostAndPort node = new HostAndPort(uri.getHost(), uri.getPort());
			nodes.add(node);
		}
		cluster = new JedisCluster(nodes, config.getConnectTimeout(), config.getSoTimeout(),
				config.getMaxRedirections(), new GenericObjectPoolConfig());
	}

	public void close() {
		try {
			cluster.close();
		} catch (IOException e) {
		}
	}

	public RedisClusterStore execute(Consumer<JedisCluster> func) {
		func.accept(cluster);
		return this;
	}

}
