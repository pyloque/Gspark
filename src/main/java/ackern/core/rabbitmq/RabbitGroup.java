package ackern.core.rabbitmq;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class RabbitGroup {

	private Map<String, RabbitStore> stores = new HashMap<>();

	public Set<String> names() {
		return stores.keySet();
	}

	public RabbitStore get(String name) {
		return stores.get(name);
	}

	public RabbitGroup register(String name, RabbitStore store) {
		stores.put(name, store);
		return this;
	}

	public RabbitGroup config(Consumer<RabbitGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
