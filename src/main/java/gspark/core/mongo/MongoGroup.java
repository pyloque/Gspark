package gspark.core.mongo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class MongoGroup {

	private Map<String, MongoStore> stores = new HashMap<>();

	public Set<String> names() {
		return stores.keySet();
	}

	public MongoGroup register(String name, MongoStore store) {
		this.stores.put(name, store);
		return this;
	}

	public MongoStore get(String name) {
		return this.stores.get(name);
	}

	public MongoGroup config(Consumer<MongoGroup> consumer) {
		consumer.accept(this);
		return this;
	}

}
