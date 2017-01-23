package ackern.core.redis;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisClusterStore {

	private JedisCluster cluster;
	
	public RedisClusterStore() {
		this(URI.create("redis://localhost:6378/0"));
	}

	public RedisClusterStore(URI... uris) {
		Set<HostAndPort> nodes = new HashSet<HostAndPort>();
		for (URI uri : uris) {
			HostAndPort node = new HostAndPort(uri.getHost(), uri.getPort());
			nodes.add(node);
		}
		cluster = new JedisCluster(nodes, 10000);
	}

	public void close() {
		try {
			cluster.close();
		} catch (IOException e) {
		}
	}

	public void execute(Consumer<JedisCluster> func) {
		func.accept(cluster);
	}

}
