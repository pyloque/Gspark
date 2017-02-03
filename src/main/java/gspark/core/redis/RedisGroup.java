package gspark.core.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class RedisGroup {

	private Map<String, RedisStore> redises = new HashMap<>();

	public Set<String> names() {
		return redises.keySet();
	}

	public RedisGroup register(String name, RedisStore store) {
		this.redises.put(name, store);
		return this;
	}

	public RedisStore redis(String name) {
		return this.redises.get(name);
	}

	public RedisGroup config(Consumer<RedisGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
