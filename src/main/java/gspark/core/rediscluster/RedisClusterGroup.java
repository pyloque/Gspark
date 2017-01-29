package gspark.core.rediscluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class RedisClusterGroup {

	private Map<String, RedisClusterStore> clusters = new HashMap<>();

	public Set<String> names() {
		return clusters.keySet();
	}

	public RedisClusterGroup register(String name, RedisClusterStore store) {
		this.clusters.put(name, store);
		return this;
	}

	public RedisClusterStore get(String name) {
		return this.clusters.get(name);
	}

	public RedisClusterGroup config(Consumer<RedisClusterGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
