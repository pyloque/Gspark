package gspark.core.codis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Singleton;

@Singleton
public class CodisGroup {

	private Map<String, CodisStore> codises = new HashMap<>();

	public Set<String> names() {
		return codises.keySet();
	}

	public CodisGroup register(String name, CodisStore store) {
		this.codises.put(name, store);
		return this;
	}

	public CodisStore get(String name) {
		return this.codises.get(name);
	}

	public CodisGroup config(Consumer<CodisGroup> configFunc) {
		configFunc.accept(this);
		return this;
	}

}
