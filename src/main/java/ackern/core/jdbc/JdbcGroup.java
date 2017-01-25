package ackern.core.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class JdbcGroup {

	private Map<String, JdbcStore> stores = new HashMap<>();

	public Set<String> names() {
		return stores.keySet();
	}

	public JdbcGroup register(String name, JdbcStore store) {
		this.stores.put(name, store);
		return this;
	}

	public JdbcStore get(String name) {
		return stores.get(name);
	}

	public JdbcGroup config(Consumer<JdbcGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
