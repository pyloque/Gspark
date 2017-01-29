package gspark.core.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

import gspark.core.codis.CodisStore;
import gspark.core.rediscluster.RedisClusterStore;

@Singleton
public class RedisGroup {

	private Map<String, RedisStore> redises = new HashMap<>();
	private Map<String, CodisStore> codises = new HashMap<>();
	private Map<String, RedisClusterStore> clusters = new HashMap<>();

	public Set<String> names() {
		return redises.keySet();
	}

	public Set<String> codisNames() {
		return codises.keySet();
	}

	public Set<String> clusterNames() {
		return clusters.keySet();
	}

	public RedisGroup register(String name, RedisStore store) {
		this.redises.put(name, store);
		return this;
	}

	public RedisGroup register(String name, CodisStore store) {
		this.codises.put(name, store);
		return this;
	}

	public RedisGroup register(String name, RedisClusterStore store) {
		this.clusters.put(name, store);
		return this;
	}

	public RedisStore redis(String name) {
		return this.redises.get(name);
	}

	public CodisStore codis(String name) {
		return this.codises.get(name);
	}

	public RedisClusterStore cluster(String name) {
		return this.clusters.get(name);
	}

	public RedisGroup config(Consumer<RedisGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
