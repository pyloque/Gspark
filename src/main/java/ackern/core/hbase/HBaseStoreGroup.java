package ackern.core.hbase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class HBaseStoreGroup {

	private Map<String, HBaseStore> stores = new HashMap<>();

	public Set<String> names() {
		return stores.keySet();
	}

	public HBaseStore get(String name) {
		return stores.get(name);
	}

	public HBaseStoreGroup register(String name, HBaseStore store) {
		stores.put(name, store);
		return this;
	}

	public HBaseStoreGroup config(Consumer<HBaseStoreGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
