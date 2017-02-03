package gspark.core.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Singleton;

@Singleton
public class HibernateGroup {

	private Map<String, HibernateStore> stores = new HashMap<>();

	public Set<String> names() {
		return stores.keySet();
	}
	
	public HibernateStore get(String name) {
		return stores.get(name);
	}

	public HibernateGroup register(String name, HibernateStore store) {
		stores.put(name, store);
		return this;
	}

	public HibernateGroup config(Consumer<HibernateGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
