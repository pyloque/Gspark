package gspark.core.memcache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class MemcacheGroup {

	private Map<String, MemcacheStore> stores = new HashMap<>();

	public Set<String> names() {
		return stores.keySet();
	}
	
	public MemcacheStore get(String name) {
		return stores.get(name);
	}

	public MemcacheGroup register(String name, MemcacheStore store) {
		stores.put(name, store);
		return this;
	}

	public MemcacheGroup config(Consumer<MemcacheGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
