package gspark.core.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class HttpClientGroup {

	private Map<String, HttpClient> clients = new HashMap<String, HttpClient>();

	public Set<String> names() {
		return clients.keySet();
	}
	
	public HttpClientGroup register(String name, HttpClient client) {
		this.clients.put(name, client);
		return this;
	}

	public HttpClient get(String name) {
		return this.clients.get(name);
	}

	public HttpClientGroup config(Consumer<HttpClientGroup> group) {
		group.accept(this);
		return this;
	}
}
